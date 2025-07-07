// 고객 이용 내역 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    let currentPage = 0; // 0부터 시작 (백엔드는 1부터 시작하므로 +1 해서 전송)
    let totalPages = 1;
    let history = [];

    // DOM 요소들
    const historyList = document.getElementById('historyList');
    const totalCount = document.getElementById('totalCount');
    const loading = document.getElementById('loading');
    const pagination = document.getElementById('pagination');

    // 초기 로드
    loadHistory();

    // 이용 내역 로드
    async function loadHistory() {
        try {
            showLoading();

            // 백엔드는 1부터 시작하는 페이지 번호를 사용
            const response = await fetch(`/api/history?page=${currentPage}&size=10`, {
                headers: {
                    'Authorization': `Bearer ${getToken()}`
                }
            });

            if (response.ok) {
                const data = await response.json();
                console.log('API 응답 데이터:', data);

                // PagedResponseDto 구조에 맞게 데이터 처리
                if (data.data && Array.isArray(data.data)) {
                    history = data.data;

                    if (data.pagination) {
                        totalPages = data.pagination.totalPages;
                        totalCount.textContent = data.pagination.totalItems;
                    } else {
                        totalPages = 1;
                        totalCount.textContent = data.data.length;
                    }
                } else {
                    // 예상하지 못한 응답 구조
                    console.error('예상하지 못한 응답 구조:', data);
                    history = [];
                    totalPages = 1;
                    totalCount.textContent = 0;
                }

                displayHistory(history);
                updatePagination();

            } else {
                const errorText = await response.text();
                console.error('API 에러 응답:', response.status, errorText);
                throw new Error(`이용 내역을 불러올 수 없습니다. (${response.status})`);
            }
        } catch (error) {
            console.error('이용 내역 로드 오류:', error);
            alert('이용 내역을 불러올 수 없습니다: ' + error.message);

            // 에러 시 빈 상태로 설정
            history = [];
            totalPages = 1;
            totalCount.textContent = 0;
            displayHistory(history);
            updatePagination();
        } finally {
            hideLoading();
        }
    }

    // 이용 내역 표시
    function displayHistory(historyData) {
        if (!historyData || !Array.isArray(historyData) || historyData.length === 0) {
            historyList.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">📋</div>
                <h3>이용 내역이 없습니다</h3>
                <p>세탁소를 이용해보세요</p>
                <a href="/customer/laundries" class="btn btn-primary">
                    🔍 세탁소 찾기
                </a>
            </div>
        `;
            return;
        }

        historyList.innerHTML = historyData.map(item => `
        <div class="history-card" data-type="${item.historyType}">
            <div class="history-header-row">
                <div class="history-type">
                    <span class="history-type-icon">${getHistoryTypeIcon(item.historyType)}</span>
                    <span>${getHistoryTypeName(item.historyType)}</span>
                </div>
                <div class="history-date">
                    📅 ${formatDateTime(item.startTime)}
                </div>
            </div>
            
            <div class="history-info">
                <div class="history-info-item">
                    <span class="info-icon">🏪</span>
                    <span>${item.laundryAddress || '주소 정보 없음'}</span>
                </div>
                <div class="history-info-item">
                    <span class="info-icon">🔧</span>
                    <span>기계 번호: #${item.machineId || 'N/A'}</span>
                </div>
            </div>
            
            <div class="history-footer">
                <div class="history-created">
                    📝 ${formatDateTime(item.createdAt)}
                </div>
            </div>
        </div>
    `).join('');
    }

    // 페이지 변경
    function changePage(page) {
        if (page < 0 || page >= totalPages) return;
        currentPage = page;
        loadHistory();
    }

    // 페이지네이션 업데이트
    function updatePagination() {
        if (totalPages <= 1) {
            pagination.innerHTML = '';
            return;
        }

        let paginationHTML = `
            <button class="page-btn" onclick="changePage(${currentPage - 1})" 
                    ${currentPage <= 0 ? 'disabled' : ''}>
                ◀ 이전
            </button>
            <span class="page-info">
                <span>${currentPage + 1}</span> / <span>${totalPages}</span>
            </span>
            <button class="page-btn" onclick="changePage(${currentPage + 1})" 
                    ${currentPage >= totalPages - 1 ? 'disabled' : ''}>
                다음 ▶
            </button>
        `;

        pagination.innerHTML = paginationHTML;
    }

    // 전역 함수로 등록
    window.changePage = changePage;

    // 내역 유형별 아이콘
    function getHistoryTypeIcon(historyType) {
        const icons = {
            'USAGE': '🔧',
            'RESERVATION': '📅',
            'CANCEL_RESERVATION': '❌'
        };
        return icons[historyType] || '📋';
    }

    // 내역 유형별 이름
    function getHistoryTypeName(historyType) {
        const names = {
            'USAGE': '사용',
            'RESERVATION': '예약',
            'CANCEL_RESERVATION': '예약 취소'
        };
        return names[historyType] || historyType;
    }

    // 상태 클래스 가져오기
    function getStatusClass(endTime) {
        if (!endTime) return 'unknown';
        const now = new Date();
        const end = new Date(endTime);
        return end <= now ? 'completed' : 'in-progress';
    }

    // 상태 텍스트 가져오기
    function getStatusText(endTime) {
        if (!endTime) return '❓ 상태 불명';
        const now = new Date();
        const end = new Date(endTime);
        return end <= now ? '✅ 완료' : '🔄 진행중';
    }

    // 소요 시간 계산
    function formatDuration(startTime, endTime) {
        if (!startTime || !endTime) return '정보 없음';
        const start = new Date(startTime);
        const end = new Date(endTime);
        const duration = Math.floor((end - start) / (1000 * 60));
        return `${duration}분`;
    }

    // 날짜 시간 포맷
    function formatDateTime(dateTimeString) {
        if (!dateTimeString) return '날짜 정보 없음';
        const date = new Date(dateTimeString);
        return date.toLocaleString('ko-KR');
    }

    // 로딩 표시/숨기기
    function showLoading() {
        if (loading) loading.style.display = 'block';
    }

    function hideLoading() {
        if (loading) loading.style.display = 'none';
    }
});


