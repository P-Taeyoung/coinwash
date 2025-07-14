// 전역 변수
let machines = [];
let selectedLaundryId = null;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 기계 관리 페이지 초기화 시작');

    initializeEventListeners();
    loadLaundries();

    console.log('✅ 기계 관리 페이지 초기화 완료');
});

// 이벤트 리스너 초기화
function initializeEventListeners() {
    // 세탁소 선택
    const laundrySelect = document.getElementById('laundrySelect');
    if (laundrySelect) {
        laundrySelect.addEventListener('change', handleLaundrySelect);
    }

    // 기계 등록 버튼
    const addMachineBtn = document.getElementById('addMachineBtn');
    if (addMachineBtn) {
        addMachineBtn.addEventListener('click', showAddMachineModal);
    }

    // 새로고침 버튼
    const refreshBtn = document.getElementById('refreshBtn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', handleRefresh);
    }

    // 상태 필터
    const statusFilter = document.getElementById('statusFilter');
    if (statusFilter) {
        statusFilter.addEventListener('change', handleStatusFilter);
    }

    // 모달 닫기 버튼들
    const closeAddModal = document.getElementById('closeAddMachineModal');
    if (closeAddModal) {
        closeAddModal.addEventListener('click', hideAddMachineModal);
    }

    const closeEditModal = document.getElementById('closeEditMachineModal');
    if (closeEditModal) {
        closeEditModal.addEventListener('click', hideEditMachineModal);
    }

    // 폼 제출
    const addMachineForm = document.getElementById('addMachineForm');
    if (addMachineForm) {
        addMachineForm.addEventListener('submit', handleAddMachine);
    }

    const editMachineForm = document.getElementById('editMachineForm');
    if (editMachineForm) {
        editMachineForm.addEventListener('submit', handleEditMachine);
    }

    // 모달 오버레이 클릭 및 ESC 키 처리
    setupModalEventHandlers();
}

// 모달 이벤트 핸들러 설정
function setupModalEventHandlers() {
    // 오버레이 클릭으로 모달 닫기
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('modal-overlay')) {
            if (document.getElementById('editMachineModal').style.display === 'flex') {
                hideEditMachineModal();
            }
            if (document.getElementById('addMachineModal').style.display === 'flex') {
                hideAddMachineModal();
            }
        }
    });

    // ESC 키로 모달 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            if (document.getElementById('editMachineModal').style.display === 'flex') {
                hideEditMachineModal();
            }
            if (document.getElementById('addMachineModal').style.display === 'flex') {
                hideAddMachineModal();
            }
        }
    });
}

// 세탁소 목록 로드
async function loadLaundries() {
    const laundrySelect = document.getElementById('laundrySelect');

    try {
        console.log('🏪 세탁소 목록 로드 시작...');
        const response = await fetch('/api/owner/laundries', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            }
        });

        if (response.ok) {
            const laundries = await response.json();
            console.log('✅ 세탁소 목록 로드 성공:', laundries);

            // 기존 옵션 제거 (첫 번째 옵션 제외)
            while (laundrySelect.children.length > 1) {
                laundrySelect.removeChild(laundrySelect.lastChild);
            }

            // 세탁소 옵션 추가
            laundries.forEach(laundry => {
                const option = document.createElement('option');
                option.value = laundry.laundryId;
                option.textContent = `${laundry.addressName}`;
                laundrySelect.appendChild(option);
            });
        } else {
            console.error('❌ 세탁소 목록 로드 실패:', response.status);
            showErrorMessage('세탁소 목록을 불러오는데 실패했습니다.');
        }
    } catch (error) {
        console.error('❌ 세탁소 목록 로드 오류:', error);
        showErrorMessage('네트워크 오류가 발생했습니다.');
    }
}

// 세탁소 선택 처리
async function handleLaundrySelect() {
    const laundrySelectValue = document.getElementById('laundrySelect').value;
    console.log('🏪 선택된 세탁소 ID (원본):', laundrySelectValue);

    const managementTools = document.getElementById('managementTools');
    const machinesSection = document.getElementById('machinesSection');

    if (!laundrySelectValue) {
        // 세탁소가 선택되지 않은 경우
        console.log('⚠️ 세탁소가 선택되지 않음');
        selectedLaundryId = null;
        window.selectedLaundryId = null;
        managementTools.style.display = 'none';
        machinesSection.style.display = 'none';
        return;
    }

    // 전역 변수에 저장 (두 가지 방식 모두)
    selectedLaundryId = laundrySelectValue;
    window.selectedLaundryId = laundrySelectValue;

    console.log('✅ 전역 변수 저장 완료:');
    console.log('  - selectedLaundryId:', selectedLaundryId);
    console.log('  - window.selectedLaundryId:', window.selectedLaundryId);

    // UI 표시
    managementTools.style.display = 'block';
    machinesSection.style.display = 'block';

    // 기계 목록 로드
    await loadMachines();
}

// 기계 목록 로드
async function loadMachines() {
    // 두 가지 방식으로 체크
    const currentLaundryId = selectedLaundryId || window.selectedLaundryId;

    console.log('🔍 기계 로드 시 세탁소 ID 체크:');
    console.log('  - selectedLaundryId:', selectedLaundryId);
    console.log('  - window.selectedLaundryId:', window.selectedLaundryId);
    console.log('  - 사용할 ID:', currentLaundryId);

    if (!currentLaundryId) {
        console.warn('⚠️ 선택된 세탁소가 없습니다.');
        return;
    }

    const loading = document.getElementById('loading');
    const machinesList = document.getElementById('machinesList');

    try {
        loading.style.display = 'block';
        machinesList.innerHTML = '';

        console.log('🔄 기계 목록 로드 시작... 세탁소 ID:', currentLaundryId);
        const response = await fetch(`/api/machines?laundryId=${currentLaundryId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            }
        });

        if (response.ok) {
            machines = await response.json();
            console.log('✅ 기계 목록 로드 성공:', machines);

            window.machinesData = machines;
            console.log('🔧 전역 변수 저장 완료:', window.machinesData);

            displayMachines(machines);
        } else {
            console.error('❌ 기계 목록 로드 실패:', response.status);
            machinesList.innerHTML = `
                <div class="error-message">
                    <p>❌ 기계 목록을 불러오는데 실패했습니다.</p>
                    <button onclick="loadMachines()" class="btn btn-primary">🔄 다시 시도</button>
                </div>
            `;
        }
    } catch (error) {
        console.error('❌ 기계 목록 로드 오류:', error);
        machinesList.innerHTML = `
            <div class="error-message">
                <p>❌ 네트워크 오류가 발생했습니다.</p>
                <button onclick="loadMachines()" class="btn btn-primary">🔄 다시 시도</button>
            </div>
        `;
    } finally {
        loading.style.display = 'none';
    }
}

// 기계 목록 표시
function displayMachines(machines) {
    const machinesList = document.getElementById('machinesList');

    if (!machines || machines.length === 0) {
        machinesList.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">🔧</div>
                <h3>등록된 기계가 없습니다</h3>
                <p>새로운 기계를 등록해보세요!</p>
                <button onclick="showAddMachineModal()" class="btn btn-primary">
                    ➕ 기계 등록하기
                </button>
            </div>
        `;
        return;
    }

    machinesList.innerHTML = machines.map(machine => `
        <div class="machine-card ${machine.usageStatus.toLowerCase()}" data-machine-id="${machine.machineId}">
            <div class="machine-header">
                <div class="machine-type">
                    ${machine.machineType === 'WASHING' ? '🌀' : '🔥'}
                    ${machine.machineType === 'WASHING' ? '세탁기' : '건조기'} #${machine.machineId}
                </div>
                <div class="machine-actions">
                    <button onclick="showEditMachineModal(${machine.machineId})" class="btn-icon" title="수정">
                        ✏️
                    </button>
                    <button onclick="deleteMachine(${machine.machineId})" class="btn-icon delete" title="삭제">
                        🗑️
                    </button>
                </div>
            </div>
            
            <div class="machine-status">
                <span class="status-badge ${machine.usageStatus.toLowerCase()}">
                    ${getStatusIcon(machine.usageStatus)} ${getStatusText(machine.usageStatus)}
                </span>
            </div>
            
            ${machine.notes ? `
                <div class="machine-notes">
                    <small>📝 ${machine.notes}</small>
                </div>
            ` : ''}
        </div>
    `).join('');
}

// 상태 필터 처리
function handleStatusFilter() {
    const filterValue = document.getElementById('statusFilter').value;
    console.log('🔍 상태 필터:', filterValue);

    if (!filterValue) {
        displayMachines(machines);
        return;
    }

    const filteredMachines = machines.filter(machine => machine.usageStatus === filterValue);
    displayMachines(filteredMachines);
}

// 기계 등록 모달 표시
function showAddMachineModal() {
    const modal = document.getElementById('addMachineModal');
    modal.style.display = 'flex';

    // 폼 초기화
    document.getElementById('addMachineForm').reset();
    document.getElementById('machineCount').value = 1;
}

// 기계 등록 모달 숨기기
function hideAddMachineModal() {
    const modal = document.getElementById('addMachineModal');
    modal.style.display = 'none';
}

// 기계 수정 모달 표시
function showEditMachineModal(machineId) {
    const machine = machines.find(m => m.machineId === machineId);
    if (!machine) {
        console.error('❌ 기계를 찾을 수 없습니다:', machineId);
        return;
    }

    console.log('✏️ 기계 수정 모달 표시:', machine);

    // 모달 필드 설정
    document.getElementById('editMachineId').value = machine.machineId;
    document.getElementById('editMachineInfo').textContent =
        `${machine.machineType === 'WASHING' ? '🌀' : '🔥'} ${machine.machineType === 'WASHING' ? '세탁기' : '건조기'} #${machine.machineId}`;
    document.getElementById('editUsageStatus').value = machine.usageStatus;
    document.getElementById('editNotes').value = machine.notes || '';

    console.log('📊 모달에 설정된 usageStatus:', machine.usageStatus);
    console.log('📊 select 요소의 현재 값:', document.getElementById('editUsageStatus').value);

    // 모달 표시
    const modal = document.getElementById('editMachineModal');
    modal.style.display = 'flex';
}

// 기계 수정 모달 숨기기
function hideEditMachineModal() {
    const modal = document.getElementById('editMachineModal');
    modal.style.display = 'none';
}

// 기계 등록 처리
async function handleAddMachine(event) {
    event.preventDefault();

    // 안전한 세탁소 ID 가져오기
    const currentLaundryId = selectedLaundryId || window.selectedLaundryId;

    console.log('➕ 기계 등록 시 세탁소 ID 체크:', currentLaundryId);

    if (!currentLaundryId) {
        console.error('❌ 세탁소가 선택되지 않았습니다.');
        showErrorMessage('세탁소를 먼저 선택해주세요.');
        return;
    }

    const formData = new FormData(event.target);
    const machineType = formData.get('machineType');
    const count = parseInt(formData.get('machineCount') || document.getElementById('machineCount').value);
    const notes = formData.get('notes') || '';

    // ✅ 백엔드 스펙에 맞게 List<MachineRegisterDto> 형태로 생성
    const machineList = [];
    for (let i = 0; i < count; i++) {
        machineList.push({
            machineType: machineType,  // "WASHING" 또는 "DRYING"
            notes: notes
        });
    }

    console.log('➕ 기계 등록 요청 데이터:');
    console.log('  - laundryId (쿼리파라미터):', currentLaundryId);
    console.log('  - machineList (body):', machineList);

    try {
        // ✅ laundryId를 쿼리 파라미터로, machineList를 body로 전송
        const response = await fetch(`/api/owner/machines?laundryId=${currentLaundryId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(machineList)  // ✅ List<MachineRegisterDto> 형태
        });

        if (response.ok) {
            const responseText = await response.text();
            console.log('✅ 기계 등록 성공!', responseText);
            hideAddMachineModal();
            await loadMachines();
            showSuccessMessage(`✅ ${count}개의 기계가 성공적으로 등록되었습니다!`);
        } else {
            const errorText = await response.text();
            console.error('❌ 기계 등록 실패:', response.status, errorText);
            showErrorMessage('기계 등록에 실패했습니다.');
        }
    } catch (error) {
        console.error('❌ 기계 등록 오류:', error);
        showErrorMessage('네트워크 오류가 발생했습니다.');
    }
}

// 기계 수정 처리 (✅ JSON 파싱 오류 해결)
async function handleEditMachine(event) {
    event.preventDefault();

    const formData = new FormData(event.target);
    const machineId = formData.get('machineId');
    const updateData = {
        machineId: formData.get('machineId'),
        usageStatus: formData.get('usageStatus'),
        notes: formData.get('notes')
    };

    console.log('✏️ 기계 수정 요청 데이터:');
    console.log('  - machineId:', machineId);
    console.log('  - updateData:', updateData);
    console.log('  - FormData 내용:');
    console.log('    - machineId:', formData.get('machineId'));
    console.log('    - usageStatus:', formData.get('usageStatus'));
    console.log('    - notes:', formData.get('notes'));

    try {
        const response = await fetch(`/api/owner/machines`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(updateData)
        });

        console.log('📡 API 응답 상태:', response.status);

        if (response.ok) {
            // ✅ 수정: 응답 타입에 따라 다르게 처리
            let responseData;
            const contentType = response.headers.get('content-type');

            if (contentType && contentType.includes('application/json')) {
                // JSON 응답인 경우
                responseData = await response.json();
                console.log('✅ 기계 수정 성공! JSON 응답:', responseData);
            } else {
                // 텍스트 응답인 경우
                responseData = await response.text();
                console.log('✅ 기계 수정 성공! 텍스트 응답:', responseData);
            }

            hideEditMachineModal();
            await loadMachines();
            showSuccessMessage('✅ 기계 정보가 성공적으로 수정되었습니다!');
        } else {
            const errorText = await response.text();
            console.error('❌ 기계 수정 실패:', response.status, errorText);
            showErrorMessage('기계 수정에 실패했습니다.');
        }
    } catch (error) {
        console.error('❌ 기계 수정 오류:', error);
        showErrorMessage('네트워크 오류가 발생했습니다.');
    }
}

// 기계 삭제
async function deleteMachine(machineId) {
    const machine = machines.find(m => m.machineId === machineId);
    if (!machine) {
        console.error('❌ 기계를 찾을 수 없습니다:', machineId);
        return;
    }

    const machineInfo = `${machine.machineType === 'WASHING' ? '세탁기' : '건조기'} #${machine.machineId}`;

    if (!confirm(`정말로 ${machineInfo}를 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.`)) {
        return;
    }

    console.log('🗑️ 기계 삭제 요청:', machineId);

    try {
        const response = await fetch(`/api/owner/machines?machineId=${machineId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            }
        });

        if (response.ok) {
            console.log('✅ 기계 삭제 성공!');
            await loadMachines();
            showSuccessMessage('✅ 기계가 성공적으로 삭제되었습니다!');
        } else {
            const errorText = await response.text();
            console.error('❌ 기계 삭제 실패:', response.status, errorText);
            showErrorMessage('기계 삭제에 실패했습니다.');
        }
    } catch (error) {
        console.error('❌ 기계 삭제 오류:', error);
        showErrorMessage('네트워크 오류가 발생했습니다.');
    }
}

// 새로고침 처리
async function handleRefresh() {
    console.log('🔄 새로고침 시작...');
    await loadMachines();
    showSuccessMessage('🔄 기계 목록이 새로고침되었습니다!');
}

// 유틸리티 함수들
function getStatusIcon(status) {
    const icons = {
        'USABLE': '✅',
        'USING': '🔄',
        'RESERVING': '⏰',
        'UNUSABLE': '❌'
    };
    return icons[status] || '❓';
}

function getStatusText(status) {
    const texts = {
        'USABLE': '사용 가능',
        'USING': '사용 중',
        'RESERVING': '예약 중',
        'UNUSABLE': '사용 불가'
    };
    return texts[status] || '알 수 없음';
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

// 메시지 표시 함수들 (간단한 알림만)
function showSuccessMessage(message) {
    alert('✅ ' + message);
}

function showErrorMessage(message) {
    alert('❌ ' + message);
}

// 전역 함수로 노출 (HTML에서 호출 가능)
window.showAddMachineModal = showAddMachineModal;
window.hideAddMachineModal = hideAddMachineModal;
window.showEditMachineModal = showEditMachineModal;
window.hideEditMachineModal = hideEditMachineModal;
window.deleteMachine = deleteMachine;
window.loadMachines = loadMachines;
