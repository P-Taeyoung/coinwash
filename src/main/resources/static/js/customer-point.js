// 고객 포인트 관리 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    let currentPoints = 0;
    let currentPage = 1;
    let totalPages = 1;

    // DOM 요소들
    const currentPointsSpan = document.getElementById('currentPoints');
    const refreshPointsBtn = document.getElementById('refreshPointsBtn');
    const pointHistoryDiv = document.getElementById('pointHistory');
    const pointPaginationDiv = document.getElementById('pointPagination');
    const loading = document.getElementById('loading');

    // 충전 관련 요소들
    const chargeBtns = document.querySelectorAll('.charge-btn');
    const customAmountInput = document.getElementById('customAmount');
    const customChargeBtn = document.getElementById('customChargeBtn');

    // 모달 관련
    const chargeModal = document.getElementById('chargeModal');
    const closeChargeModal = document.getElementById('closeChargeModal');
    const confirmChargeBtn = document.getElementById('confirmCharge');
    const chargeAmountSpan = document.getElementById('chargeAmount');

    let selectedAmount = 0;

    // 이벤트 리스너들
    if (refreshPointsBtn) {
        refreshPointsBtn.addEventListener('click', () => {
            loadCurrentPoints();
            loadPointHistory(1);
        });
    }

    if (closeChargeModal) {
        closeChargeModal.addEventListener('click', hideChargeModal);
    }

    if (confirmChargeBtn) {
        confirmChargeBtn.addEventListener('click', handleChargePoints);
    }

    if (customChargeBtn) {
        customChargeBtn.addEventListener('click', handleCustomCharge);
    }

    // 충전 금액 버튼 이벤트
    chargeBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            selectedAmount = parseInt(this.dataset.amount);
            showChargeModal(selectedAmount);
        });
    });

    // 커스텀 금액 충전 처리
    function handleCustomCharge() {
        const amount = parseInt(customAmountInput.value);

        if (!amount || amount <= 0) {
            alert('충전할 금액을 입력해주세요.');
            return;
        }

        if (amount < 1000) {
            alert('최소 충전 금액은 1,000원입니다.');
            return;
        }

        if (amount > 100000) {
            alert('최대 충전 금액은 100,000원입니다.');
            return;
        }

        selectedAmount = amount;
        showChargeModal(amount);
    }

    // 초기 로드
    loadCurrentPoints();
    loadPointHistory();

    // 현재 포인트 로드
    async function loadCurrentPoints() {
        try {
            const response = await fetch('/api/customer', {
                headers: {
                    'Authorization': `Bearer ${getToken()}`
                }
            });

            if (response.ok) {
                const customer = await response.json();
                currentPoints = customer.points;
                if (currentPointsSpan) {
                    currentPointsSpan.textContent = currentPoints.toLocaleString() + '원';
                }
            } else {
                throw new Error('포인트 정보를 불러올 수 없습니다.');
            }
        } catch (error) {
            console.error('포인트 로드 오류:', error);
        }
    }

    // 포인트 내역 로드
    async function loadPointHistory(page = 1) {
        try {
            showLoading();

            const url = `/api/point?page=${page - 1}&limit=10`;
            console.log('요청 URL:', url);

            const response = await fetch(url, {
                headers: {
                    'Authorization': `Bearer ${getToken()}`
                }
            });

            if (!response.ok) {
                throw new Error('포인트 내역을 불러오는데 실패했습니다.');
            }

            const result = await response.json();
            console.log('포인트 내역 응답:', result);

            // 페이지 정보 업데이트
            if (result && result.pagination) {
                currentPage = page;
                totalPages = result.pagination.totalPages;
            }

            console.log('페이지 정보 - 현재:', currentPage, '총:', totalPages);

            // 데이터 표시
            if (result && result.data) {
                displayPointHistory(result.data);
            } else {
                console.error('응답 데이터 형식이 올바르지 않습니다:', result);
                displayPointHistory([]);
            }

            // 페이지네이션 업데이트
            updatePagination();

        } catch (error) {
            console.error('포인트 내역 로딩 오류:', error);
            showError('포인트 내역을 불러오는데 실패했습니다.');
            displayPointHistory([]);
        } finally {
            hideLoading();
        }
    }

    // 포인트 내역 표시
    function displayPointHistory(data) {
        const tbody = document.querySelector('#pointHistory .table tbody');

        if (!tbody) {
            console.error('테이블 tbody를 찾을 수 없습니다.');
            return;
        }

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3" class="text-center">포인트 내역이 없습니다.</td></tr>';
            return;
        }

        tbody.innerHTML = data.map(item => {
            const date = new Date(item.createdAt).toLocaleDateString('ko-KR');

            // pointType 매핑 처리
            let typeText, typeClass, pointText, pointClass;

            if (item.pointType === 'EARNED') {
                typeText = '충전';
                typeClass = 'bg-success';
                pointText = `+${item.changedPoint.toLocaleString()}P`;
                pointClass = 'text-success';
            } else if (item.pointType === 'USED') {
                typeText = '사용';
                typeClass = 'bg-danger';
                pointText = `-${Math.abs(item.changedPoint).toLocaleString()}P`;
                pointClass = 'text-danger';
            } else {
                typeText = item.pointType;
                typeClass = 'bg-secondary';
                pointText = `${item.changedPoint.toLocaleString()}P`;
                pointClass = '';
            }

            return `
                <tr>
                    <td>${date}</td>
                    <td><span class="badge ${typeClass}">${typeText}</span></td>
                    <td><span class="${pointClass}">${pointText}</span></td>
                </tr>
            `;
        }).join('');
    }

    // 포인트 충전 모달 표시
    function showChargeModal(amount) {
        if (chargeModal && chargeAmountSpan) {
            chargeAmountSpan.textContent = amount.toLocaleString();
            chargeModal.style.display = 'block';
        }
    }

    // 포인트 충전 모달 숨기기
    function hideChargeModal() {
        if (chargeModal) {
            chargeModal.style.display = 'none';
        }
    }

    // 포인트 충전 처리
    async function handleChargePoints() {
        if (selectedAmount <= 0) {
            alert('충전할 금액을 선택해주세요.');
            return;
        }

        try {
            const response = await fetch(`/api/point?points=${selectedAmount}`, {
                method: 'PATCH',
                headers: {
                    'Authorization': `Bearer ${getToken()}`
                }
            });

            if (response.ok) {
                const message = await response.text();
                alert(message);
                hideChargeModal();
                loadCurrentPoints();
                loadPointHistory(1);

                if (customAmountInput) {
                    customAmountInput.value = '';
                }
                selectedAmount = 0;
            } else {
                const error = await response.text();
                alert(error || '포인트 충전에 실패했습니다.');
            }
        } catch (error) {
            console.error('포인트 충전 오류:', error);
            alert('포인트 충전에 실패했습니다.');
        }
    }

    // 페이지네이션 업데이트
    function updatePagination() {
        console.log('updatePagination 호출됨');
        console.log('pointPaginationDiv:', pointPaginationDiv);
        console.log('currentPage:', currentPage);
        console.log('totalPages:', totalPages);

        if (!pointPaginationDiv) {
            console.error('pointPaginationDiv를 찾을 수 없습니다');
            return;
        }

        let paginationHTML = '';

        if (totalPages > 1) {
            console.log('페이지네이션 HTML 생성 중...');
            paginationHTML = `
            <div class="d-flex justify-content-center align-items-center gap-2 mt-3">
                <button class="btn btn-sm btn-outline-primary" 
                        ${currentPage <= 1 ? 'disabled' : ''} 
                        onclick="changePage(${currentPage - 1})">
                    이전
                </button>
                
                <span class="mx-3">
                    <strong>${currentPage}</strong> / ${totalPages} 페이지
                </span>
                
                <button class="btn btn-sm btn-outline-primary" 
                        ${currentPage >= totalPages ? 'disabled' : ''} 
                        onclick="changePage(${currentPage + 1})">
                    다음
                </button>
            </div>
        `;
        } else {
            console.log('totalPages가 1 이하입니다. 페이지네이션을 표시하지 않습니다.');
        }

        pointPaginationDiv.innerHTML = paginationHTML;
        console.log('페이지네이션 HTML 설정 완료:', paginationHTML);
    }

    // 페이지 변경 (전역 함수)
    window.changePage = function(page) {
        console.log('changePage 호출됨, page:', page);
        if (page < 1 || page > totalPages) {
            console.log('유효하지 않은 페이지:', page);
            return;
        }
        loadPointHistory(page);
    };

    // 로딩 표시/숨기기
    function showLoading() {
        if (loading) loading.style.display = 'block';
    }

    function hideLoading() {
        if (loading) loading.style.display = 'none';
    }

    // 에러 표시
    function showError(message) {
        alert(message);
    }

});
