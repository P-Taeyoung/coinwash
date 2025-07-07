// 헤더 관련 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    setTimeout(() => {
        updateHeader();
        setupMobileMenu();
        setupTokenChangeListener(); // 🔧 토큰 변경 감지 설정
    }, 100);
});

// 🔧 토큰 변경 감지 리스너 설정
function setupTokenChangeListener() {
    // localStorage 변경 감지 (다른 탭에서의 로그인/로그아웃)
    window.addEventListener('storage', (e) => {
        if (e.key === 'token') {
            console.log('🔔 토큰 변경 감지:', e.oldValue ? '있음' : '없음', '->', e.newValue ? '있음' : '없음');

            // 🔧 안전한 접근
            if (window.notificationManager) {
                if (e.newValue && !e.oldValue) {
                    // 토큰이 새로 생성됨 (로그인)
                    console.log('🔔 로그인 감지 - SSE 연결 시작');
                    window.notificationManager.onLogin();
                } else if (e.oldValue && !e.newValue) {
                    // 토큰이 제거됨 (로그아웃)
                    console.log('🔔 로그아웃 감지 - SSE 연결 해제');
                    window.notificationManager.onLogout();
                }
            } else {
                console.warn('🔔 notificationManager가 아직 로드되지 않았습니다.');
            }
        }
    });

    // 🔧 현재 탭에서의 토큰 변경 감지
    window.addEventListener('tokenChanged', (e) => {
        // 🔧 안전한 접근
        if (window.notificationManager) {
            const { action, token } = e.detail;
            console.log('🔔 현재 탭 토큰 변경:', action);

            if (action === 'set' && token) {
                console.log('🔔 현재 탭 로그인 감지 - SSE 연결 시작');
                window.notificationManager.onLogin();
            } else if (action === 'remove') {
                console.log('🔔 현재 탭 로그아웃 감지 - SSE 연결 해제');
                window.notificationManager.onLogout();
            }
        } else {
            console.warn('🔔 notificationManager가 아직 로드되지 않았습니다.');
        }
    });
}

// 개선된 헤더 업데이트 함수
async function updateHeader() {
    const token = getToken();
    const navMenu = document.getElementById('navMenu');
    const mobileMenu = document.getElementById('mobileMenu');

    if (!navMenu || !mobileMenu) {
        console.warn('헤더 요소를 찾을 수 없습니다. 다시 시도합니다...');
        setTimeout(updateHeader, 200);
        return;
    }

    if (!token) {
        renderGuestHeader(navMenu, mobileMenu);
        return;
    }

    try {
        // 토큰에서 역할만 확인 (최소한의 정보만)
        const role = getRoleFromToken(token);

        if (!role) {
            removeToken();
            renderGuestHeader(navMenu, mobileMenu);
            return;
        }

        // 역할에 따라 적절한 API 호출해서 실제 사용자 정보 조회
        const userInfo = await getUserInfoByRole(role);

        if (!userInfo) {
            removeToken();
            renderGuestHeader(navMenu, mobileMenu);
            return;
        }

        // 역할에 따른 헤더 렌더링
        if (role === 'CUSTOMER') {
            renderCustomerHeader(navMenu, mobileMenu, userInfo.name);
        } else if (role === 'OWNER') {
            renderOwnerHeader(navMenu, mobileMenu, userInfo.name);
        } else {
            renderGuestHeader(navMenu, mobileMenu);
        }

    } catch (error) {
        console.error('사용자 정보 조회 실패:', error);
        removeToken();
        renderGuestHeader(navMenu, mobileMenu);
    }

    setupDropdownListeners();
}

// 역할에 따른 사용자 정보 조회 (백엔드 API 호출)
async function getUserInfoByRole(role) {
    try {
        let response;

        if (role === 'CUSTOMER') {
            response = await authenticatedFetch('/api/customer');
        } else if (role === 'OWNER') {
            response = await authenticatedFetch('/api/owner');
        } else {
            return null;
        }

        if (response.ok) {
            return await response.json();
        } else if (response.status === 401) {
            // 401은 authenticatedFetch에서 처리하므로 여기서는 null 반환
            return null;
        } else {
            console.error('사용자 정보 조회 실패:', response.status);
            return null;
        }

    } catch (error) {
        console.error('API 호출 오류:', error);
        return null;
    }
}

// 토큰에서 역할만 추출 (보안 개선)
function getRoleFromToken(token) {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));

        // 토큰 만료 시간 확인
        const currentTime = Math.floor(Date.now() / 1000);
        if (payload.exp && payload.exp < currentTime) {
            return null;
        }

        // 역할만 반환 (다른 민감한 정보는 백엔드에서 조회)
        return payload.role ||
            (payload.authorities && payload.authorities[0] ?
                payload.authorities[0].replace('ROLE_', '') : null);
    } catch (e) {
        console.error('토큰 파싱 오류:', e);
        return null;
    }
}

// 비로그인 헤더 렌더링
function renderGuestHeader(navMenu, mobileMenu) {
    if (!navMenu || !mobileMenu) return;

    navMenu.innerHTML = `
        <div class="nav-items">
            <a href="/" class="nav-link">🏠 홈</a>
            <a href="/auth/signin" class="nav-link">🚪 로그인</a>
            <div class="nav-dropdown">
                <button class="nav-dropdown-btn">📝 회원가입 ▼</button>
                <div class="nav-dropdown-content">
                    <a href="/customer/signup">👤 고객 회원가입</a>
                    <a href="/owner/signup">🏪 점주 회원가입</a>
                </div>
            </div>
        </div>
    `;

    mobileMenu.innerHTML = `
        <div class="mobile-nav-items">
            <a href="/" class="mobile-nav-link">🏠 홈</a>
            <a href="/auth/signin" class="mobile-nav-link">🚪 로그인</a>
            <a href="/customer/signup" class="mobile-nav-link">👤 고객 회원가입</a>
            <a href="/owner/signup" class="mobile-nav-link">🏪 점주 회원가입</a>
        </div>
    `;
}

// 고객 헤더 렌더링
function renderCustomerHeader(navMenu, mobileMenu, userName) {
    if (!navMenu || !mobileMenu) return;

    navMenu.innerHTML = `
        <div class="nav-items">
            <a href="/" class="nav-link">🏠 홈</a>
            <a href="/customer/laundries" class="nav-link">🔍 세탁소 찾기</a>
            <a href="/customer/history" class="nav-link">📋 이용 내역</a>
            <a href="/customer/point" class="nav-link">💰 포인트</a>
            
            <!-- 🔔 알림 아이콘 추가 -->
            <div class="nav-notification" id="navNotification">
                <button class="notification-icon-btn" id="notificationBtn">
                    🔔
                    <span class="notification-badge hidden" id="notificationBadge">0</span>
                </button>
                
                <!-- 알림 드롭다운 -->
                <div class="notification-dropdown" id="notificationDropdown">
                    <div class="notification-dropdown-header">
                        <h3 class="notification-dropdown-title">알림</h3>
                        <button class="notification-clear-btn" id="clearAllBtn">모두 지우기</button>
                    </div>
                    <div class="notification-list" id="notificationList">
                        <!-- 알림 항목들이 여기에 동적으로 추가됩니다 -->
                    </div>
                </div>
            </div>
            
            <div class="nav-dropdown">
                <button class="nav-dropdown-btn">👤 ${userName} ▼</button>
                <div class="nav-dropdown-content">
                    <a href="/customer/profile">👤 내 정보</a>
                    <a href="#" onclick="logout()">🚪 로그아웃</a>
                </div>
            </div>
        </div>
    `;

    mobileMenu.innerHTML = `
        <div class="mobile-nav-items">
            <a href="/" class="mobile-nav-link">🏠 홈</a>
            <a href="/customer/laundries" class="mobile-nav-link">🔍 세탁소 찾기</a>
            <a href="/customer/history" class="mobile-nav-link">📋 이용 내역</a>
            <a href="/customer/point" class="mobile-nav-link">💰 포인트</a>
            <a href="/customer/profile" class="mobile-nav-link">👤 내 정보</a>
            <a href="#" onclick="logout()" class="mobile-nav-link">🚪 로그아웃</a>
        </div>
    `;

    // 🔔 알림 이벤트 리스너 설정
    setupNotificationListeners();
}

// 점주 헤더 렌더링
function renderOwnerHeader(navMenu, mobileMenu, userName) {
    if (!navMenu || !mobileMenu) return;

    navMenu.innerHTML = `
        <div class="nav-items">
            <a href="/" class="nav-link">🏠 홈</a>
            <a href="/owner/laundries" class="nav-link">🏪 세탁소 관리</a>
            <a href="/owner/machines" class="nav-link">🔧 기계 관리</a>
            
            <!-- 🔔 알림 아이콘 추가 -->
            <div class="nav-notification" id="navNotification">
                <button class="notification-icon-btn" id="notificationBtn">
                    🔔
                    <span class="notification-badge hidden" id="notificationBadge">0</span>
                </button>
                
                <!-- 알림 드롭다운 -->
                <div class="notification-dropdown" id="notificationDropdown">
                    <div class="notification-dropdown-header">
                        <h3 class="notification-dropdown-title">알림</h3>
                        <button class="notification-clear-btn" id="clearAllBtn">모두 지우기</button>
                    </div>
                    <div class="notification-list" id="notificationList">
                        <!-- 알림 항목들이 여기에 동적으로 추가됩니다 -->
                    </div>
                </div>
            </div>
            
            <div class="nav-dropdown">
                <button class="nav-dropdown-btn">🏪 ${userName} ▼</button>
                <div class="nav-dropdown-content">
                    <a href="/owner/profile">👤 내 정보</a>
                    <a href="#" onclick="logout()">🚪 로그아웃</a>
                </div>
            </div>
        </div>
    `;

    mobileMenu.innerHTML = `
        <div class="mobile-nav-items">
            <a href="/" class="mobile-nav-link">🏠 홈</a>
            <a href="/owner/laundries" class="mobile-nav-link">🏪 세탁소 관리</a>
            <a href="/owner/machines" class="mobile-nav-link">🔧 기계 관리</a>
            <a href="/owner/profile" class="mobile-nav-link">👤 내 정보</a>
            <a href="#" onclick="logout()" class="mobile-nav-link">🚪 로그아웃</a>
        </div>
    `;

    // 🔔 알림 이벤트 리스너 설정
    setupNotificationListeners();
}

// 🔔 알림 이벤트 리스너 설정 함수
function setupNotificationListeners() {
    setTimeout(() => {
        const notificationBtn = document.getElementById('notificationBtn');
        const clearAllBtn = document.getElementById('clearAllBtn');

        if (notificationBtn) {
            notificationBtn.replaceWith(notificationBtn.cloneNode(true));
            const newNotificationBtn = document.getElementById('notificationBtn');

            newNotificationBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                // 🔧 수정된 접근 방법
                if (window.notificationManager) {
                    window.notificationManager.toggleNotificationDropdown();
                }
            });
        }

        if (clearAllBtn) {
            clearAllBtn.replaceWith(clearAllBtn.cloneNode(true));
            const newClearAllBtn = document.getElementById('clearAllBtn');

            newClearAllBtn.addEventListener('click', () => {
                // 🔧 수정된 접근 방법
                if (window.notificationManager) {
                    window.notificationManager.clearAllNotifications();
                }
            });
        }

        // 알림 항목 클릭 이벤트
        document.addEventListener('click', (e) => {
            if (e.target.closest('.notification-item')) {
                const item = e.target.closest('.notification-item');
                const notificationId = item.dataset.notificationId;
                if (window.notificationManager && notificationId) {
                    window.notificationManager.markAsRead(notificationId);
                }
            }
        });

        // 오버레이 클릭 이벤트
        const overlay = document.getElementById('notificationOverlay');
        if (overlay) {
            overlay.addEventListener('click', () => {
                if (window.notificationManager) {
                    window.notificationManager.closeNotificationDropdown();
                }
            });
        }

        // 🔔 SSE 연결 시작 (중복 방지 강화)
        if (window.notificationManager) {
            console.log('🔔 SSE 연결 시도 중...');
            window.notificationManager.connectSSE();
        } else {
            console.warn('🔔 notificationManager가 로드되지 않았습니다.');
            // 🔧 잠시 후 다시 시도
            setTimeout(() => {
                if (window.notificationManager) {
                    console.log('🔔 지연된 SSE 연결 시도...');
                    window.notificationManager.connectSSE();
                }
            }, 500);
        }
    }, 100);
}

// 모바일 메뉴 설정
function setupMobileMenu() {
    const mobileMenuBtn = document.getElementById('mobileMenuBtn');
    const mobileMenu = document.getElementById('mobileMenu');

    if (mobileMenuBtn && mobileMenu) {
        mobileMenuBtn.addEventListener('click', function() {
            mobileMenu.classList.toggle('active');

            if (mobileMenu.classList.contains('active')) {
                mobileMenuBtn.textContent = '✕';
            } else {
                mobileMenuBtn.textContent = '☰';
            }
        });

        document.addEventListener('click', function(e) {
            if (!mobileMenuBtn.contains(e.target) && !mobileMenu.contains(e.target)) {
                mobileMenu.classList.remove('active');
                mobileMenuBtn.textContent = '☰';
            }
        });
    }
}

// 드롭다운 리스너 설정
function setupDropdownListeners() {
    const dropdownBtns = document.querySelectorAll('.nav-dropdown-btn');

    dropdownBtns.forEach(btn => {
        if (!btn.hasAttribute('data-listener-added')) {
            btn.setAttribute('data-listener-added', 'true');
            btn.addEventListener('click', function(e) {
                e.stopPropagation();
                const dropdown = this.parentElement;
                const content = dropdown.querySelector('.nav-dropdown-content');

                document.querySelectorAll('.nav-dropdown-content').forEach(other => {
                    if (other !== content) {
                        other.style.display = 'none';
                    }
                });

                if (content.style.display === 'block') {
                    content.style.display = 'none';
                } else {
                    content.style.display = 'block';
                }
            });
        }
    });

    document.addEventListener('click', function() {
        document.querySelectorAll('.nav-dropdown-content').forEach(content => {
            content.style.display = 'none';
        });
    });
}

// 🔧 로그아웃 함수 수정 (로직 복구)
async function logout() {
    try {
        // 🔔 SSE 연결 해제 (안전한 접근)
        if (window.notificationManager) {
            await window.notificationManager.disconnectSSE();
        }

        // 3. 🔧 알림 관련 데이터 삭제
        localStorage.removeItem('notificationHistory');

        // 토큰 제거
        removeToken();

        // 헤더 업데이트 (비로그인 상태로)
        updateHeader();

        // 성공 메시지
        alert('로그아웃되었습니다.');

        // 홈페이지로 리다이렉트
        window.location.href = '/';

    } catch (error) {
        console.error('로그아웃 처리 중 오류:', error);

        // 오류가 발생해도 로그아웃 처리는 진행
        removeToken();
        updateHeader();
        window.location.href = '/';
    }
}

// 토큰 관리 함수들
function getToken() {
    return localStorage.getItem('token') || sessionStorage.getItem('token');
}

// 🔧 setToken 함수 - 쿠키 설정 제거 (auth-signin.js에서 담당)
function setToken(token, remember = false) {
    const cleanToken = token.startsWith('Bearer ') ? token.substring(7) : token;

    console.log('🔧 header.js setToken 호출됨:', { token: token.substring(0, 20) + '...', remember });

    if (remember) {
        localStorage.setItem('token', cleanToken);
        sessionStorage.removeItem('token');
    } else {
        sessionStorage.setItem('token', cleanToken);
        localStorage.removeItem('token');
    }

    // 🔧 쿠키는 auth-signin.js에서 설정하므로 여기서는 제거
    // 이벤트만 발생
    window.dispatchEvent(new CustomEvent('tokenChanged', {
        detail: { action: 'set', token: cleanToken }
    }));
}

// 🔧 removeToken 함수 - 쿠키 삭제는 유지
function removeToken() {
    // 로컬스토리지와 세션스토리지에서 토큰 삭제
    localStorage.removeItem('token');
    sessionStorage.removeItem('token');

    // 🔧 쿠키 삭제
    document.cookie = 'token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT; SameSite=Lax;';

    // 현재 탭에서 토큰 제거 이벤트 발생
    window.dispatchEvent(new CustomEvent('tokenChanged', {
        detail: { action: 'remove', token: null }
    }));
}

// 인증된 요청을 위한 fetch 함수
async function authenticatedFetch(url, options = {}) {
    const token = getToken();

    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
            ...(token && { 'Authorization': `Bearer ${token}` })
        }
    };

    const mergedOptions = {
        ...defaultOptions,
        ...options,
        headers: {
            ...defaultOptions.headers,
            ...options.headers
        }
    };

    try {
        const response = await fetch(url, mergedOptions);

        if (response.status === 401) {
            removeToken();
            updateHeader();
            alert('로그인이 만료되었습니다. 다시 로그인해주세요.');
            window.location.href = '/auth/signin';
            return;
        }

        return response;
    } catch (error) {
        console.error('API 요청 오류:', error);
        throw error;
    }
}

// 로그인 상태 확인
function isAuthenticated() {
    const token = getToken();
    return token && getRoleFromToken(token) !== null;
}

// 로그인 성공 시 호출할 함수
function onLoginSuccess(token, remember = false) {
    setToken(token, remember); // 🔧 이제 setToken에서 자동으로 이벤트 발생
    updateHeader(); // 헤더 업데이트

    console.log('🔐 로그인 성공 - 헤더 업데이트 완료');
    // SSE 연결은 tokenChanged 이벤트에서 자동으로 처리됨
}

// 전역 함수로 제공
window.updateHeader = updateHeader;
window.logout = logout;
window.setToken = setToken;
window.removeToken = removeToken
