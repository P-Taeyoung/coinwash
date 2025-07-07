// 고객 기계 관리 JavaScript
let machines = [];
let userPoints = 0;
let currentUserId = null; // 현재 사용자 ID 저장

// 사용자 정보 로드
async function loadUserInfo() {
    try {
        const response = await fetch('/api/customer', {
            headers: {
                'Authorization': `Bearer ${getToken()}`
            }
        });

        if (response.ok) {
            const user = await response.json();
            console.log('사용자 정보:', user); // 디버깅용

            userPoints = user.points;
            currentUserId = user.customerId; // 사용자 ID 저장

            console.log('설정된 currentUserId:', currentUserId); // 디버깅용
            updatePointsDisplay(); // 포인트 표시 업데이트
        }
    } catch (error) {
        console.error('사용자 정보 로드 오류:', error);
    }
}

// 포인트 표시 업데이트 함수
function updatePointsDisplay() {
    const userPointsElement = document.getElementById('userPoints');
    if (userPointsElement) {
        userPointsElement.textContent = userPoints.toLocaleString();
    }
}

document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const laundryId = urlParams.get('laundryId');

    if (!laundryId) {
        alert('잘못된 접근입니다.');
        window.location.href = '/customer/laundries';
        return;
    }

    // DOM 요소들
    const refreshBtn = document.getElementById('refreshBtn');
    const machinesGrid = document.getElementById('machinesGrid');
    const loading = document.getElementById('loading');

    // 모달 관련
    const useMachineModal = document.getElementById('useMachineModal');
    const reserveMachineModal = document.getElementById('reserveMachineModal');
    const closeUseMachineModal = document.getElementById('closeUseMachineModal');
    const closeReserveMachineModal = document.getElementById('closeReserveMachineModal');

    // 이벤트 리스너
    refreshBtn.addEventListener('click', loadMachines);
    closeUseMachineModal.addEventListener('click', () => useMachineModal.style.display = 'none');
    closeReserveMachineModal.addEventListener('click', () => reserveMachineModal.style.display = 'none');

    // 초기 로드 - 순서 중요!
    initializePage();

    // 페이지 초기화 함수
    async function initializePage() {
        try {
            showLoading();
            // 1. 먼저 사용자 정보 로드
            await loadUserInfo();
            // 2. 그 다음 기계 정보 로드
            await loadMachines();
        } catch (error) {
            console.error('페이지 초기화 오류:', error);
            alert('페이지를 불러오는 중 오류가 발생했습니다.');
        } finally {
            hideLoading();
        }
    }

    // 기계 목록 로드
    async function loadMachines() {
        try {
            const response = await fetch(`/api/machines?laundryId=${laundryId}`, {
                headers: {
                    'Authorization': `Bearer ${getToken()}`
                }
            });

            if (response.ok) {
                machines = await response.json();
                displayMachines(machines);
                updateSummary(machines);
            } else {
                throw new Error('기계 정보를 불러올 수 없습니다.');
            }
        } catch (error) {
            console.error('기계 로드 오류:', error);
            alert('기계 정보를 불러올 수 없습니다.');
        }
    }

    // 기계 목록 표시
    function displayMachines(machines) {
        if (machines.length === 0) {
            machinesGrid.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">🔧</div>
                <h3>등록된 기계가 없습니다</h3>
                <p>세탁소에 등록된 기계가 없습니다</p>
            </div>
        `;
            return;
        }

        // 디버깅용 로그 추가
        console.log('현재 사용자 ID:', currentUserId, typeof currentUserId);

        machinesGrid.innerHTML = machines.map(machine => {
            const statusInfo = getStatusInfo(machine.usageStatus);
            const isUsable = machine.usageStatus === 'USABLE';

            // 디버깅용 로그 추가
            if (machine.usageStatus === 'RESERVING') {
                console.log('예약 중인 기계:', {
                    machineId: machine.machineId,
                    customerId: machine.customerId,
                    customerIdType: typeof machine.customerId,
                    currentUserId: currentUserId,
                    currentUserIdType: typeof currentUserId,
                    isEqual: machine.customerId === currentUserId,
                    strictEqual: machine.customerId === Number(currentUserId)
                });
            }

            // 타입 안전한 비교
            const isMyReservation = machine.usageStatus === 'RESERVING' &&
                currentUserId !== null &&
                String(machine.customerId) === String(currentUserId);

            const canUse = isUsable || isMyReservation;
            const canReserve = machine.usageStatus === 'USABLE';

            return `
            <div class="machine-card ${machine.usageStatus.toLowerCase()} ${isMyReservation ? 'my-reservation' : ''}">
                <div class="machine-header">
                    <div class="machine-type">
                        ${machine.machineType === 'WASHING' ? '🌀 세탁기' : '🔥 건조기'}
                    </div>
                    <div class="machine-status ${statusInfo.class}">
                        ${statusInfo.text}
                        ${isMyReservation ? ' (내 예약)' : ''}
                    </div>
                </div>
                
                <div class="machine-info">
                    <p class="machine-id">기계 번호: #${machine.machineId}</p>
                    ${machine.endTime ? `<p class="machine-time">⏰ 종료 예정: ${formatDateTime(machine.endTime)}</p>` : ''}
                    ${machine.notes ? `<p class="machine-notes">📝 ${machine.notes}</p>` : ''}
                    ${isMyReservation ? `<p class="my-reservation-info">👤 내가 예약한 기계입니다</p>` : ''}
                </div>
                
                <div class="machine-actions">
                    ${canUse ? `
                        <button class="btn btn-primary" onclick="showUseMachineModal(${machine.machineId}, '${machine.machineType}')">
                            ✨ 사용하기
                        </button>
                        ${canReserve ? `
                            <button class="btn btn-outline" onclick="showReserveMachineModal(${machine.machineId}, '${machine.machineType}')">
                                📅 예약하기
                            </button>
                        ` : ''}
                        ${isMyReservation ? `
                            <button class="btn btn-danger" onclick="cancelReservation(${machine.machineId})">
                                ❌ 예약 취소
                            </button>
                        ` : ''}
                    ` : `
                        <button class="btn btn-secondary" disabled>
                            ${statusInfo.actionText}
                        </button>
                    `}
                </div>
            </div>
        `;
        }).join('');
    }

    // 현황 요약 업데이트
    function updateSummary(machines) {
        const washing = machines.filter(m => m.machineType === 'WASHING').length;
        const drying = machines.filter(m => m.machineType === 'DRYING').length;
        const available = machines.filter(m => m.usageStatus === 'USABLE').length;
        const inUse = machines.filter(m => m.usageStatus === 'USING').length;

        document.getElementById('washingCount').textContent = washing;
        document.getElementById('dryingCount').textContent = drying;
        document.getElementById('availableCount').textContent = available;
        document.getElementById('inUseCount').textContent = inUse;
    }

    // 상태 정보 가져오기
    function getStatusInfo(status) {
        const statusMap = {
            'USABLE': { text: '✅ 사용 가능', class: 'available', actionText: '사용 가능' },
            'USING': { text: '🔄 사용 중', class: 'in-use', actionText: '사용 중' },
            'RESERVING': { text: '📅 예약 중', class: 'reserved', actionText: '예약 중' },
            'UNUSABLE': { text: '❌ 사용 불가', class: 'unavailable', actionText: '사용 불가' }
        };
        return statusMap[status] || { text: '❓ 알 수 없음', class: 'unknown', actionText: '알 수 없음' };
    }

    // 날짜 시간 포맷
    function formatDateTime(dateTimeString) {
        const date = new Date(dateTimeString);
        return date.toLocaleString('ko-KR');
    }

    // 로딩 표시/숨기기
    function showLoading() {
        loading.style.display = 'block';
    }

    function hideLoading() {
        loading.style.display = 'none';
    }

    // loadMachines 함수를 전역으로 노출
    window.loadMachines = loadMachines;

    // 전역 함수들 (window 객체에 추가)
    window.showUseMachineModal = showUseMachineModal;
    window.showReserveMachineModal = showReserveMachineModal;
    window.cancelReservation = cancelReservation;
});

// 예약 취소 함수
async function cancelReservation(machineId) {
    if (!confirm('예약을 취소하시겠습니까?')) {
        return;
    }

    try {
        const response = await fetch(`/api/machines/reservations?machineId=${machineId}`, {
            method: 'PATCH',
            headers: {
                'Authorization': `Bearer ${getToken()}`
            }
        });

        if (response.ok) {
            const message = await response.text();
            alert(message);
            await window.loadMachines(); // 기계 상태 업데이트
        } else {
            const error = await response.text();
            alert(error || '예약 취소에 실패했습니다.');
        }
    } catch (error) {
        console.error('예약 취소 오류:', error);
        alert('예약 취소에 실패했습니다.');
    }
}

// 기계 사용 모달 표시
function showUseMachineModal(machineId, machineType) {
    const modal = document.getElementById('useMachineModal');
    const form = document.getElementById('useMachineForm');
    const title = document.getElementById('useMachineTitle');
    const courseOptions = document.getElementById('courseOptions');

    // 모달 제목 설정
    title.textContent = machineType === 'WASHING' ? '🌀 세탁기 사용' : '🔥 건조기 사용';

    // 기계 ID 설정
    document.getElementById('useMachineId').value = machineId;
    document.getElementById('machineType').value = machineType;

    // 보유 포인트 표시
    document.getElementById('userPoints').textContent = userPoints.toLocaleString();

    // 코스 옵션 생성
    const courses = getCourseOptions(machineType);
    courseOptions.innerHTML = courses.map(course => `
        <label class="course-option">
            <input type="radio" name="course" value="${course.value}" data-points="${course.points}">
            <div class="course-info">
                <div class="course-name">${course.name}</div>
                <div class="course-price">${course.points}원</div>
                <div class="course-description">${course.description}</div>
            </div>
        </label>
    `).join('');

    // 코스 선택 이벤트
    courseOptions.querySelectorAll('input[name="course"]').forEach(radio => {
        radio.addEventListener('change', function() {
            if (this.checked) {
                const selectedPoints = parseInt(this.dataset.points);
                document.getElementById('selectedCourse').textContent = this.parentElement.querySelector('.course-name').textContent;
                document.getElementById('requiredPoints').textContent = selectedPoints.toLocaleString();

                // 포인트 부족 여부 확인 및 표시
                const remainingPoints = userPoints - selectedPoints;
                document.getElementById('remainingPoints').textContent = remainingPoints.toLocaleString();

                // 포인트 부족시 경고 표시
                const pointsWarning = document.getElementById('pointsWarning');
                if (remainingPoints < 0) {
                    pointsWarning.style.display = 'block';
                    pointsWarning.textContent = `포인트가 ${Math.abs(remainingPoints).toLocaleString()}원 부족합니다.`;
                } else {
                    pointsWarning.style.display = 'none';
                }
            }
        });
    });

    // 폼 제출 이벤트
    form.onsubmit = handleUseMachine;

    modal.style.display = 'block';
}

// 기계 예약 모달 표시
function showReserveMachineModal(machineId, machineType) {
    const modal = document.getElementById('reserveMachineModal');
    const form = document.getElementById('reserveMachineForm');

    document.getElementById('reserveMachineId').value = machineId;

    // 폼 제출 이벤트
    form.onsubmit = handleReserveMachine;

    modal.style.display = 'block';
}

// 코스 옵션 가져오기
function getCourseOptions(machineType) {
    if (machineType === 'WASHING') {
        return [
            { value: 'WASHING_A_COURSE', name: '표준 세탁', points: 4000, description: '일반적인 세탁 (30분)' },
            { value: 'WASHING_B_COURSE', name: '강력 세탁', points: 5000, description: '강력한 세탁 (45분)' },
            { value: 'WASHING_C_COURSE', name: '울 세탁', points: 6000, description: '울 전용 세탁 (40분)' }
        ];
    } else {
        return [
            { value: 'DRYING_A_COURSE', name: '표준 건조', points: 3000, description: '일반적인 건조 (30분)' },
            { value: 'DRYING_B_COURSE', name: '강력 건조', points: 4000, description: '강력한 건조 (45분)' },
            { value: 'DRYING_C_COURSE', name: '저온 건조', points: 5000, description: '저온 건조 (50분)' }
        ];
    }
}

// 기계 사용 처리
async function handleUseMachine(e) {
    e.preventDefault();

    const formData = new FormData(e.target);
    const machineId = formData.get('machineId');
    const machineType = formData.get('machineType');
    const course = formData.get('course');

    if (!course) {
        alert('코스를 선택해주세요.');
        return;
    }

    // 선택된 코스의 포인트 확인
    const selectedCourseElement = document.querySelector('input[name="course"]:checked');
    const requiredPoints = parseInt(selectedCourseElement.dataset.points);

    if (userPoints < requiredPoints) {
        alert(`포인트가 부족합니다. (보유: ${userPoints.toLocaleString()}원, 필요: ${requiredPoints.toLocaleString()}원)`);
        return;
    }

    // 확인 메시지
    const confirmMessage = `${machineType === 'WASHING' ? '세탁기' : '건조기'}를 사용하시겠습니까?\n\n` +
        `• 보유 포인트: ${userPoints.toLocaleString()}원\n` +
        `• 필요 포인트: ${requiredPoints.toLocaleString()}원\n` +
        `• 사용 후 포인트: ${(userPoints - requiredPoints).toLocaleString()}원`;

    if (!confirm(confirmMessage)) {
        return;
    }

    try {
        const endpoint = machineType === 'WASHING' ? '/api/machines/washing' : '/api/machines/drying';
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${getToken()}`
            },
            body: JSON.stringify({
                machineId: parseInt(machineId),
                course: course
            })
        });

        if (response.ok) {
            const message = await response.text();
            alert(message);
            document.getElementById('useMachineModal').style.display = 'none';

            // 포인트와 기계 상태 동시 업데이트
            await Promise.all([
                loadUserInfo(),      // 포인트 업데이트
                window.loadMachines() // 기계 상태 업데이트
            ]);
        } else {
            const error = await response.text();
            alert(error || '기계 사용에 실패했습니다.');
        }
    } catch (error) {
        console.error('기계 사용 오류:', error);
        alert('기계 사용에 실패했습니다.');
    }
}

// 기계 예약 처리
async function handleReserveMachine(e) {
    e.preventDefault();

    const formData = new FormData(e.target);
    const machineId = formData.get('machineId');

    try {
        const response = await fetch(`/api/machines/reservations?machineId=${machineId}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${getToken()}`
            }
        });

        if (response.ok) {
            const message = await response.text();
            alert(message);
            document.getElementById('reserveMachineModal').style.display = 'none';

            // 기계 상태 업데이트
            await window.loadMachines();
        } else {
            const error = await response.text();
            alert(error || '기계 예약에 실패했습니다.');
        }
    } catch (error) {
        console.error('기계 예약 오류:', error);
        alert('기계 예약에 실패했습니다.');
    }
}

