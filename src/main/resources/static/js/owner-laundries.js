// 점주 세탁소 관리 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    let laundries = [];

    // DOM 요소들
    const addLaundryBtn = document.getElementById('addLaundryBtn');
    const refreshBtn = document.getElementById('refreshBtn');
    const laundriesList = document.getElementById('laundriesList');
    const loading = document.getElementById('loading');
    const totalCount = document.getElementById('totalCount');

    // 모달 관련
    const laundryModal = document.getElementById('laundryModal');
    const statusModal = document.getElementById('statusModal');
    const closeLaundryModal = document.getElementById('closeLaundryModal');
    const closeStatusModal = document.getElementById('closeStatusModal');
    const laundryForm = document.getElementById('laundryForm');

    // 이벤트 리스너
    addLaundryBtn.addEventListener('click', showAddLaundryModal);
    refreshBtn.addEventListener('click', loadLaundries);
    closeLaundryModal.addEventListener('click', hideLaundryModal);
    closeStatusModal.addEventListener('click', hideStatusModal);
    laundryForm.addEventListener('submit', handleLaundrySubmit);

    // search-address.js 초기화 (모달에서 사용)
    function initModalAddressSearch() {
        if (typeof initAddressSearch === 'function') {
            initAddressSearch();
        }
    }

    // 초기 로드
    loadLaundries();

    // 세탁소 목록 로드
    async function loadLaundries() {
        try {
            showLoading();
            console.log('🏪 세탁소 목록 로드 시작...');

            const response = await fetch('/api/owner/laundries', {
                headers: {
                    'Authorization': `Bearer ${getToken()}`
                }
            });

            console.log('📡 응답 상태:', response.status);

            if (response.ok) {
                laundries = await response.json();
                console.log('✅ 세탁소 목록 로드 성공:', laundries);
                displayOwnerLaundries(laundries);

                window.laundries = laundries;

                if (totalCount) {
                    totalCount.textContent = laundries.length;
                }
            } else {
                const errorText = await response.text();
                console.error('❌ 세탁소 목록 로드 실패:', response.status, errorText);
                throw new Error(`세탁소 목록을 불러올 수 없습니다. (${response.status})`);
            }
        } catch (error) {
            console.error('세탁소 로드 오류:', error);
            showNotification('세탁소 목록을 불러올 수 없습니다.', 'error');

            laundriesList.innerHTML = `
                <div class="empty-state error-state">
                    <div class="empty-icon">⚠️</div>
                    <h3>로드 실패</h3>
                    <p>세탁소 목록을 불러올 수 없습니다.</p>
                    <button class="btn btn-primary" onclick="loadLaundries()">
                        🔄 다시 시도
                    </button>
                </div>
            `;
        } finally {
            hideLoading();
        }
    }

    // 🎨 세탁소 목록 표시 (디자인 개선, 좌표 정보 제거)
    function displayOwnerLaundries(laundries) {
        if (!laundries || laundries.length === 0) {
            laundriesList.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">🏪</div>
                <h3>등록된 세탁소가 없습니다</h3>
                <p>첫 번째 세탁소를 등록해보세요</p>
                <button class="btn btn-primary" onclick="showAddLaundryModal()">
                    ➕ 세탁소 등록
                </button>
            </div>
        `;
            return;
        }

        laundriesList.innerHTML = laundries.map(laundry => `
        <div class="laundry-card" data-laundry-id="${laundry.laundryId}">
            <div class="laundry-card-header">
                <div class="laundry-title">
                    <h3 class="laundry-name">
                        <span class="laundry-icon">🏪</span>
                        세탁소 #${laundry.laundryId}
                    </h3>
                    <div class="laundry-status-badge ${laundry.opened ? 'status-open' : 'status-closed'}">
                        <span class="status-dot"></span>
                        ${laundry.opened ? '영업중' : '영업종료'}
                    </div>
                </div>
            </div>
            
            <div class="laundry-card-body">
                <div class="laundry-info-item">
                    <span class="info-icon">📍</span>
                    <span class="info-text">${laundry.addressName}</span>
                </div>
                
                ${laundry.description ? `
                    <div class="laundry-info-item">
                        <span class="info-icon">💬</span>
                        <span class="info-text">${laundry.description}</span>
                    </div>
                ` : ''}
            </div>
            
            <div class="laundry-card-footer">
                <div class="action-buttons">
                    <button class="btn btn-secondary" onclick="editLaundry(${laundry.laundryId})" title="세탁소 정보 수정">
                        <span class="btn-icon">✏️</span>
                        수정
                    </button>
                    <button class="btn ${laundry.opened ? 'btn-warning' : 'btn-success'}" 
                            onclick="toggleLaundryStatus(${laundry.laundryId}, ${laundry.opened})"
                            title="${laundry.opened ? '영업 중단' : '영업 시작'}">
                        <span class="btn-icon">${laundry.opened ? '⏸️' : '▶️'}</span>
                        ${laundry.opened ? '영업중단' : '영업시작'}
                    </button>
                    <button class="btn btn-primary" onclick="viewMachines(${laundry.laundryId})" title="세탁기 관리">
                        <span class="btn-icon">🔧</span>
                        기계관리
                    </button>
                    <button class="btn btn-danger" onclick="deleteLaundry(${laundry.laundryId})" title="세탁소 삭제">
                        <span class="btn-icon">🗑️</span>
                        삭제
                    </button>
                </div>
            </div>
        </div>
    `).join('');

        console.log(`✅ ${laundries.length}개 세탁소 표시 완료`);
    }

    // 세탁소 등록 모달 표시
    function showAddLaundryModal() {
        document.getElementById('modalTitle').textContent = '➕ 세탁소 등록';
        document.getElementById('laundryId').value = '';
        laundryForm.reset();

        if (typeof clearSelectedAddress === 'function') {
            clearSelectedAddress();
        }

        if (typeof hideAddressResults === 'function') {
            hideAddressResults();
        }

        laundryModal.style.display = 'flex';

        setTimeout(() => {
            initModalAddressSearch();
        }, 100);

        console.log('➕ 세탁소 등록 모달 열림');
    }

    // 세탁소 수정 모달 표시
    function showEditLaundryModal(laundry) {
        document.getElementById('modalTitle').textContent = '✏️ 세탁소 수정';
        document.getElementById('laundryId').value = laundry.laundryId;
        document.getElementById('description').value = laundry.description || '';

        // ✅ 주소 검색 관련 숨기기
        document.getElementById('addressSearchSection').style.display = 'none';
        document.getElementById('selectedAddressSection').style.display = 'none';

        laundryModal.style.display = 'flex'; // ✅ 'block' → 'flex'로 변경

        console.log('✏️ 세탁소 수정 모달 열림:', laundry);
    }

    // 모달 숨기기
    function hideLaundryModal() {
        laundryModal.style.display = 'none';

        if (typeof clearSelectedAddress === 'function') {
            clearSelectedAddress();
        }
        if (typeof hideAddressResults === 'function') {
            hideAddressResults();
        }
    }

    function hideStatusModal() {
        statusModal.style.display = 'none';
    }

    // 세탁소 등록/수정 처리
    async function handleLaundrySubmit(e) {
        e.preventDefault();

        const formData = new FormData(laundryForm);
        const laundryId = formData.get('laundryId');
        const isEdit = laundryId && laundryId.trim() !== '';

        console.log('📝 폼 제출:', isEdit ? '수정' : '등록');

        // ✅ 등록일 때만 주소 필수 검증
        if (!isEdit) {
            const addressName = formData.get('addressName');
            if (!addressName || addressName.trim() === '') {
                showNotification('주소를 선택해주세요.', 'error');
                return;
            }
        }

        try {
            let url, method, body;

            if (isEdit) {
                // 수정 요청
                url = `/api/owner/laundries?laundryId=${laundryId}`;  
                method = 'PATCH';
                body = JSON.stringify({
                    description: formData.get('description') || ''
                });
            } else {
                // 등록 요청
                url = '/api/owner/laundries';
                method = 'POST';
                body = JSON.stringify({
                    addressName: formData.get('addressName'),
                    latitude: parseFloat(formData.get('latitude')),
                    longitude: parseFloat(formData.get('longitude')),
                    description: formData.get('description') || ''
                });
            }

            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${getToken()}`
                },
                body: body
            });

            if (response.ok) {
                const message = await response.text();
                console.log(`✅ 세탁소 ${isEdit ? '수정' : '등록'} 성공:`, message);
                showNotification(message, 'success');
                hideLaundryModal();
                loadLaundries();
            } else {
                const error = await response.text();
                console.error(`❌ 세탁소 ${isEdit ? '수정' : '등록'} 실패:`, error);
                showNotification(error || `세탁소 ${isEdit ? '수정' : '등록'}에 실패했습니다.`, 'error');
            }
        } catch (error) {
            console.error(`세탁소 ${isEdit ? '수정' : '등록'} 오류:`, error);
            showNotification(`세탁소 ${isEdit ? '수정' : '등록'}에 실패했습니다.`, 'error');
        }
    }

    // 로딩 표시/숨기기
    function showLoading() {
        if (loading) {
            loading.style.display = 'block';
        }
    }

    function hideLoading() {
        if (loading) {
            loading.style.display = 'none';
        }
    }

    // 전역 함수들
    window.showAddLaundryModal = showAddLaundryModal;
    window.showEditLaundryModal = showEditLaundryModal;
    window.editLaundry = editLaundry;
    window.toggleLaundryStatus = toggleLaundryStatus;
    window.viewMachines = viewMachines;
    window.loadLaundries = loadLaundries;
});

// 나머지 함수들은 동일...
async function editLaundry(laundryId) {
    try {
        console.log('✏️ 세탁소 수정 요청:', laundryId);

        const laundry = window.laundries?.find(l => l.laundryId === laundryId);

        if (laundry) {
            console.log('✅ 캐시된 데이터 사용:', laundry);
            showEditLaundryModal(laundry);
        } else {
            console.log('❌ 캐시된 데이터 없음, API 호출');
            showNotification('세탁소 정보를 찾을 수 없습니다. 목록을 새로고침해주세요.', 'error');
        }
    } catch (error) {
        console.error('세탁소 정보 로드 오류:', error);
        showNotification('세탁소 정보를 불러올 수 없습니다.', 'error');
    }
}

async function toggleLaundryStatus(laundryId, currentStatus) {
    const action = currentStatus ? '영업을 중단' : '영업을 시작';

    if (!confirm(`정말로 ${action}하시겠습니까?`)) {
        return;
    }

    try {
        console.log('🔄 상태 변경 요청:', { laundryId, currentStatus });

        const response = await fetch(`/api/owner/laundries/status?laundryId=${laundryId}`, {
            method: 'PATCH',
            headers: {
                'Authorization': `Bearer ${getToken()}`
            }
        });

        if (response.ok) {
            const message = await response.text();
            console.log('✅ 상태 변경 성공:', message);
            showNotification(message, 'success');
            loadLaundries();
        } else {
            const error = await response.text();
            console.error('❌ 상태 변경 실패:', error);
            showNotification(error || '상태 변경에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('상태 변경 오류:', error);
        showNotification('상태 변경에 실패했습니다.', 'error');
    }
}

function viewMachines(laundryId) {
    console.log('🔧 기계 관리 페이지로 이동:', laundryId);
    window.location.href = `/owner/machines?laundryId=${laundryId}`;
}

// 세탁소 삭제
async function deleteLaundry(laundryId) {
    if (!confirm('⚠️ 정말로 이 세탁소를 삭제하시겠습니까?\n\n삭제된 세탁소는 복구할 수 없습니다.')) {
        return;
    }

    try {
        console.log('🗑️ 세탁소 삭제 요청:', laundryId);

        const response = await fetch(`/api/owner/laundries?laundryId=${laundryId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${getToken()}`
            }
        });

        if (response.ok) {
            const message = await response.text();
            console.log('✅ 세탁소 삭제 성공:', message);
            showNotification(message, 'success');
            loadLaundries(); // 목록 새로고침
        } else {
            const error = await response.text();
            console.error('❌ 세탁소 삭제 실패:', error);
            showNotification(error || '세탁소 삭제에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('세탁소 삭제 오류:', error);
        showNotification('세탁소 삭제에 실패했습니다.', 'error');
    }
}

// 전역 함수에 추가
window.deleteLaundry = deleteLaundry;


