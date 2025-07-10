// notification.js - SSE 기반 실시간 알림 시스템 (최종 개선 버전)
class NotificationManager {
    constructor() {
        this.container = null;
        this.notifications = [];
        this.notificationHistory = [];
        this.maxNotifications = 5;
        this.eventSource = null;
        this.unreadCount = 0;
        this.isConnected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.loginCheckInterval = null;
        this.loginCheckDelay = 5000;
        this.init();
        this.loadNotificationHistory();
    }

    init() {
        // 토스트 알림 컨테이너 생성
        this.container = document.createElement('div');
        this.container.className = 'notification-container';
        document.body.appendChild(this.container);
    }

    // 🔧 로그인 상태 확인 함수
    isUserLoggedIn() {
        const token = localStorage.getItem('token') || sessionStorage.getItem('token');

        if (!token) {
            return false;
        }

        // 🔒 토큰 만료 확인
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const currentTime = Math.floor(Date.now() / 1000);

            if (payload.exp && payload.exp < currentTime) {
                console.log('🔔 토큰이 만료되었습니다.');
                this.clearExpiredTokens();
                return false;
            }

            return true;
        } catch (error) {
            console.error('🔔 토큰 파싱 오류:', error);
            this.clearExpiredTokens();
            return false;
        }
    }

    // 🔧 만료된/잘못된 토큰 제거
    clearExpiredTokens() {
        localStorage.removeItem('token');
        sessionStorage.removeItem('token');
        document.cookie = 'token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
        console.log('🔔 만료된 토큰을 제거했습니다.');
    }

    // 🔧 로그인 상태 주기적 체크 시작
    startLoginCheck() {
        console.log('🔔 로그인 상태 주기적 체크 시작...');

        if (this.loginCheckInterval) {
            clearInterval(this.loginCheckInterval);
        }

        this.loginCheckInterval = setInterval(() => {
            if (this.isUserLoggedIn()) {
                console.log('🔔 로그인 감지! SSE 연결을 시작합니다.');
                this.stopLoginCheck();
                this.connectSSE();
            }
        }, this.loginCheckDelay);
    }

    // 🔧 로그인 상태 체크 중단
    stopLoginCheck() {
        if (this.loginCheckInterval) {
            clearInterval(this.loginCheckInterval);
            this.loginCheckInterval = null;
            console.log('🔔 로그인 상태 체크를 중단했습니다.');
        }
    }

    // 알림 히스토리 로컬 저장소에서 로드
    loadNotificationHistory() {
        try {
            const saved = localStorage.getItem('notificationHistory');
            if (saved) {
                this.notificationHistory = JSON.parse(saved);
                this.updateUnreadCount();
                setTimeout(() => {
                    this.updateHeaderNotification();
                }, 1000);
            }
        } catch (error) {
            console.error('알림 히스토리 로드 오류:', error);
            this.notificationHistory = [];
        }
    }

    // 알림 히스토리 로컬 저장소에 저장
    saveNotificationHistory() {
        try {
            localStorage.setItem('notificationHistory', JSON.stringify(this.notificationHistory));
        } catch (error) {
            console.error('알림 히스토리 저장 오류:', error);
        }
    }

    // 🔧 수정된 SSE 연결 시작
    connectSSE() {
        console.log('🔔 SSE 연결 시도...');

        if (!this.isUserLoggedIn()) {
            console.log('🔔 로그인되지 않아 SSE 연결을 시작하지 않습니다.');
            this.startLoginCheck();
            return;
        }

        this.stopLoginCheck();

        if (this.eventSource) {
            if (this.eventSource.readyState === EventSource.OPEN) {
                console.log('🔔 이미 SSE가 연결되어 있습니다.');
                return;
            } else if (this.eventSource.readyState === EventSource.CONNECTING) {
                console.log('🔔 SSE 연결 중입니다. 잠시 기다려주세요.');
                return;
            } else {
                console.log('🔔 기존 SSE 연결 정리 중...');
                this.eventSource.close();
                this.eventSource = null;
            }
        }

        try {
            console.log('🔔 새로운 SSE 연결 시작...');

            this.eventSource = new EventSource('/api/sse/subscribe', {
                withCredentials: true
            });

            this.eventSource.onopen = () => {
                console.log('🔔 SSE 연결 성공!');
                this.isConnected = true;
                this.reconnectAttempts = 0;
            };

            // 🔧 일반 메시지 리스너 (기존)
            this.eventSource.onmessage = (event) => {
                try {
                    console.log('📨 일반 SSE 메시지 수신:', event.data);
                    const data = JSON.parse(event.data);
                    this.handleServerNotification(data);
                } catch (error) {
                    console.error('일반 알림 데이터 파싱 오류:', error);
                    console.log('받은 데이터:', event.data);
                }
            };

            // 🔧 특정 이벤트 타입 리스너 추가
            this.eventSource.addEventListener('notification', (event) => {
                try {
                    console.log('📨 notification 이벤트 수신:', event.data);
                    const data = JSON.parse(event.data);
                    this.handleServerNotification(data);
                } catch (error) {
                    console.error('notification 이벤트 파싱 오류:', error);
                    console.log('받은 데이터:', event.data);
                }
            });

            // 🔧 연결 이벤트 리스너
            this.eventSource.addEventListener('connect', (event) => {
                try {
                    console.log('📨 connect 이벤트 수신:', event.data);
                    const data = JSON.parse(event.data);
                    console.log('연결 정보:', data);
                } catch (error) {
                    console.error('connect 이벤트 파싱 오류:', error);
                }
            });

            // 🔧 하트비트 이벤트 리스너
            this.eventSource.addEventListener('heartbeat', (event) => {
                console.log('💓 하트비트 수신');
            });

            this.eventSource.onerror = (error) => {
                console.error('🔔 SSE 연결 오류:', error);
                this.isConnected = false;

                if (this.eventSource && this.eventSource.readyState === EventSource.CLOSED) {
                    this.showToast('error', '연결 끊김', '알림 서비스 연결이 끊어졌습니다.', 5000);

                    if (this.isUserLoggedIn() && this.reconnectAttempts < this.maxReconnectAttempts) {
                        this.reconnectAttempts++;
                        const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000);

                        console.log(`🔔 SSE 자동 재연결 시도 ${this.reconnectAttempts}/${this.maxReconnectAttempts} (${delay}ms 후)...`);

                        setTimeout(() => {
                            if (!this.isConnected) {
                                this.connectSSE();
                            }
                        }, delay);
                    } else if (!this.isUserLoggedIn()) {
                        console.log('🔔 로그아웃 상태로 인해 재연결을 중단하고 로그인 체크를 시작합니다.');
                        this.startLoginCheck();
                    } else {
                        this.showToast('error', '재연결 실패', '알림 서비스 재연결에 실패했습니다. 페이지를 새로고침해주세요.', 10000);
                    }
                }
            };

        } catch (error) {
            console.error('🔔 SSE 연결 생성 오류:', error);
            this.showToast('error', '연결 실패', '알림 서비스에 연결할 수 없습니다.', 5000);
        }
    }

    // 🔧 수정된 SSE 연결 해제
    async disconnectSSE() {
        console.log('🔔 SSE 연결 해제 시작...');

        this.stopLoginCheck();

        if (this.eventSource) {
            this.eventSource.close();
            this.eventSource = null;
            this.isConnected = false;
            console.log('🔔 클라이언트 SSE 연결 해제');
        }

        // 🔧 로그인 상태일 때만 서버에 해제 요청
        if (this.isUserLoggedIn()) {
            try {
                const token = localStorage.getItem('token') || sessionStorage.getItem('token');
                const response = await fetch('/api/sse/unsubscribe', {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    },
                    credentials: 'include'
                });

                if (response.ok) {
                    const message = await response.text();
                    console.log('🔔 서버 SSE 연결 해제 완료:', message);
                } else {
                    console.warn('SSE 연결 해제 응답 오류:', response.status);
                }
            } catch (error) {
                console.error('SSE 연결 해제 요청 실패:', error);
            }
        }
    }

    // 🔧 로그아웃 시 호출할 메서드
    onLogout() {
        console.log('🔔 로그아웃 감지 - SSE 연결 해제');
        this.disconnectSSE();
    }

    // 🔧 로그인 시 호출할 메서드
    onLogin() {
        console.log('🔔 로그인 감지 - SSE 연결 시작');
        this.connectSSE();
    }

    // 🔧 개선된 서버 알림 처리
    handleServerNotification(data) {
        console.log('📨 서버 알림 수신 상세:', {
            원본데이터: data,
            데이터타입: typeof data,
            키목록: Object.keys(data || {}),
            JSON문자열: JSON.stringify(data)
        });

        // 🔧 데이터 구조 검증
        if (!data) {
            console.error('❌ 알림 데이터가 null 또는 undefined입니다.');
            return;
        }

        const { type, title, message, duration, id } = data;

        console.log('📨 파싱된 알림 정보:', {
            type: type,
            title: title,
            message: message,
            duration: duration,
            id: id
        });

        // 🔧 필수 필드 검증
        if (!message) {
            console.error('❌ 메시지가 없는 알림입니다:', data);
            return;
        }

        this.showToast(type || 'info', title || '알림', message, duration || 5000);

        this.addToHistory({
            id: id || Date.now().toString(),
            type: type || 'info',
            title: title || '알림',
            message: message,
            timestamp: new Date().toISOString(),
            read: false
        });
    }

    // 🔧 수정된 재연결 메서드
    reconnect() {
        console.log('🔔 SSE 수동 재연결 시도...');

        if (!this.isUserLoggedIn()) {
            console.log('🔔 로그인되지 않아 재연결할 수 없습니다.');
            return;
        }

        this.disconnectSSE();
        this.reconnectAttempts = 0;
        setTimeout(() => {
            this.connectSSE();
        }, 1000);
    }

    // 🔧 정리 메서드
    cleanup() {
        console.log('🔔 NotificationManager 정리 중...');
        this.stopLoginCheck();
        this.disconnectSSE();
    }

    // 연결 상태 확인
    isSSEConnected() {
        return this.eventSource &&
            this.eventSource.readyState === EventSource.OPEN &&
            this.isConnected;
    }

    // 연결 상태 표시
    getConnectionStatus() {
        if (!this.isUserLoggedIn()) {
            return { status: 'logged-out', message: '로그인 필요' };
        } else if (this.isSSEConnected()) {
            return { status: 'connected', message: '연결됨' };
        } else if (this.reconnectAttempts > 0) {
            return { status: 'reconnecting', message: '재연결 중...' };
        } else {
            return { status: 'disconnected', message: '연결 안됨' };
        }
    }

    // 🔧 히스토리에 알림 추가
    addToHistory(notification) {
        if (this.notificationHistory.some(n => n.id === notification.id)) {
            console.log('중복 알림 무시:', notification.id);
            return;
        }

        this.notificationHistory.unshift(notification);

        if (this.notificationHistory.length > 100) {
            this.notificationHistory = this.notificationHistory.slice(0, 100);
        }

        this.saveNotificationHistory();
        this.updateUnreadCount();
        this.updateHeaderNotification();
        this.updateNotificationList();
    }

    updateUnreadCount() {
        this.unreadCount = this.notificationHistory.filter(n => !n.read).length;
    }

    updateHeaderNotification() {
        const badge = document.getElementById('notificationBadge');
        if (badge) {
            if (this.unreadCount > 0) {
                badge.textContent = this.unreadCount > 99 ? '99+' : this.unreadCount;
                badge.classList.remove('hidden');
            } else {
                badge.classList.add('hidden');
            }
        }
    }

    toggleNotificationDropdown() {
        const dropdown = document.getElementById('notificationDropdown');
        const overlay = document.getElementById('notificationOverlay');

        if (dropdown && overlay) {
            const isVisible = dropdown.classList.contains('show');

            if (isVisible) {
                this.closeNotificationDropdown();
            } else {
                this.openNotificationDropdown();
            }
        }
    }

    openNotificationDropdown() {
        const dropdown = document.getElementById('notificationDropdown');
        const overlay = document.getElementById('notificationOverlay');

        if (dropdown && overlay) {
            dropdown.classList.add('show');
            overlay.classList.add('show');
            this.updateNotificationList();
        }
    }

    closeNotificationDropdown() {
        const dropdown = document.getElementById('notificationDropdown');
        const overlay = document.getElementById('notificationOverlay');

        if (dropdown && overlay) {
            dropdown.classList.remove('show');
            overlay.classList.remove('show');
        }
    }

    updateNotificationList() {
        const notificationList = document.getElementById('notificationList');
        if (!notificationList) return;

        if (this.notificationHistory.length === 0) {
            notificationList.innerHTML = `
                <div class="notification-empty">
                    <div class="notification-empty-icon">🔔</div>
                    <h3>알림이 없습니다</h3>
                    <p>새로운 알림이 오면 여기에 표시됩니다.</p>
                </div>
            `;
            return;
        }

        notificationList.innerHTML = this.notificationHistory.map(notification => {
            const timeAgo = this.getTimeAgo(new Date(notification.timestamp));
            return `
                <div class="notification-item ${!notification.read ? 'unread' : ''}" 
                     data-notification-id="${notification.id}">
                    <div class="notification-item-header">
                        <h4 class="notification-item-title">${this.escapeHtml(notification.title)}</h4>
                        <span class="notification-item-time">${timeAgo}</span>
                    </div>
                    <p class="notification-item-message">${this.escapeHtml(notification.message)}</p>
                    <span class="notification-item-type ${notification.type}">${this.getTypeLabel(notification.type)}</span>
                </div>
            `;
        }).join('');
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    markAsRead(notificationId) {
        const notification = this.notificationHistory.find(n => n.id === notificationId);
        if (notification && !notification.read) {
            notification.read = true;
            this.saveNotificationHistory();
            this.updateUnreadCount();
            this.updateHeaderNotification();
            this.updateNotificationList();
        }
    }

    clearAllNotifications() {
        if (confirm('모든 알림을 삭제하시겠습니까?')) {
            this.notificationHistory = [];
            this.unreadCount = 0;
            this.saveNotificationHistory();
            this.updateHeaderNotification();
            this.updateNotificationList();
            this.closeNotificationDropdown();
            this.showToast('info', '알림 삭제', '모든 알림이 삭제되었습니다.', 2000);
        }
    }

    getTimeAgo(date) {
        const now = new Date();
        const diff = now - date;
        const minutes = Math.floor(diff / 60000);
        const hours = Math.floor(diff / 3600000);
        const days = Math.floor(diff / 86400000);

        if (minutes < 1) return '방금 전';
        if (minutes < 60) return `${minutes}분 전`;
        if (hours < 24) return `${hours}시간 전`;
        if (days < 7) return `${days}일 전`;
        return date.toLocaleDateString();
    }

    getTypeLabel(type) {
        const labels = {
            success: '성공',
            info: '정보',
            warning: '경고',
            error: '오류'
        };
        return labels[type] || '알림';
    }

    showToast(type, title, message, duration = 5000) {
        const notification = this.createToastNotification(type, title, message);

        if (this.notifications.length >= this.maxNotifications) {
            this.removeOldest();
        }

        this.container.appendChild(notification);
        this.notifications.push(notification);

        setTimeout(() => {
            notification.classList.add('show');
        }, 100);

        if (duration > 0) {
            setTimeout(() => {
                this.remove(notification);
            }, duration);
        }

        return notification;
    }

    createToastNotification(type, title, message) {
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;

        const iconMap = {
            success: '✓',
            info: 'ℹ',
            warning: '⚠',
            error: '✕'
        };

        notification.innerHTML = `
            <div class="notification-header">
                <div style="display: flex; align-items: center;">
                    <div class="notification-icon">${iconMap[type] || 'ℹ'}</div>
                    <div class="notification-title">${this.escapeHtml(title)}</div>
                </div>
                <button class="notification-close" onclick="window.notificationManager.remove(this.closest('.notification'))">×</button>
            </div>
            <p class="notification-message">${this.escapeHtml(message)}</p>
            <div class="notification-time">${new Date().toLocaleTimeString()}</div>
        `;

        return notification;
    }

    remove(notification) {
        if (!notification || !notification.parentNode) return;

        notification.classList.add('hide');

        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
                this.notifications = this.notifications.filter(n => n !== notification);
            }
        }, 300);
    }

    removeOldest() {
        if (this.notifications.length > 0) {
            this.remove(this.notifications[0]);
        }
    }

    // 🔧 편의 메서드들
    success(title, message, duration) {
        return this.showToast('success', title, message, duration);
    }

    info(title, message, duration) {
        return this.showToast('info', title, message, duration);
    }

    warning(title, message, duration) {
        return this.showToast('warning', title, message, duration);
    }

    error(title, message, duration) {
        return this.showToast('error', title, message, duration);
    }

    clear() {
        this.notifications.forEach(notification => {
            this.remove(notification);
        });
    }

    // 🔧 디버깅 메서드 추가
    getDebugInfo() {
        return {
            isLoggedIn: this.isUserLoggedIn(),
            isSSEConnected: this.isSSEConnected(),
            eventSourceState: this.eventSource?.readyState,
            reconnectAttempts: this.reconnectAttempts,
            unreadCount: this.unreadCount,
            historyCount: this.notificationHistory.length,
            connectionStatus: this.getConnectionStatus()
        };
    }
}

// 🔧 전역 인스턴스 생성 및 등록
window.notificationManager = new NotificationManager();

// 🔧 하위 호환성을 위한 별칭
window.NotificationManager = NotificationManager;

// 페이지 언로드 시 연결 해제
window.addEventListener('beforeunload', () => {
    if (window.notificationManager) {
        window.notificationManager.cleanup();
    }
});

// 🔧 페이지 포커스 시 연결 상태 확인
window.addEventListener('focus', () => {
    if (window.notificationManager && window.notificationManager.isUserLoggedIn()) {
        if (!window.notificationManager.isSSEConnected()) {
            console.log('페이지 포커스 시 SSE 재연결 시도 (로그인 상태)');
            window.notificationManager.reconnect();
        } else {
            console.log('페이지 포커스 - SSE 이미 연결됨');
        }
    } else {
        console.log('페이지 포커스 - 로그인되지 않아 SSE 재연결 시도하지 않음');
    }
});

// 🔧 디버깅을 위한 전역 함수
window.debugNotification = () => {
    console.log('🔍 알림 시스템 디버그 정보:', window.notificationManager.getDebugInfo());
};

console.log('🔔 NotificationManager 로드 완료');
