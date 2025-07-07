// 점주 프로필 페이지 JavaScript (고객 프로필과 유사하지만 점주용으로 수정)
document.addEventListener('DOMContentLoaded', function() {
    loadOwnerProfile();
    setupEventListeners();
    setupAnimations();
});

let currentUserData = null;

// 점주 프로필 정보 로드
async function loadOwnerProfile() {
    const loadingState = document.getElementById('loadingState');
    const errorState = document.getElementById('errorState');
    const ownerProfile = document.getElementById('ownerProfile');

    try {
        const response = await authenticatedFetch('/api/owner');

        if (!response.ok) {
            throw new Error('점주 정보를 불러올 수 없습니다');
        }

        const userData = await response.json();
        currentUserData = userData;

        // UI 업데이트
        displayOwnerProfile(userData);

        loadingState.style.display = 'none';
        errorState.style.display = 'none';
        ownerProfile.style.display = 'block';

        // 애니메이션 시작
        triggerAnimations();

    } catch (error) {
        console.error('프로필 로드 오류:', error);
        loadingState.style.display = 'none';
        errorState.style.display = 'block';
    }
}

// 점주 프로필 표시
function displayOwnerProfile(data) {
    document.getElementById('ownerName').textContent = data.name || '-';
    document.getElementById('ownerPhone').textContent = data.phone || '-';
}

// 나머지 함수들은 고객 프로필과 동일하되, API 엔드포인트만 '/api/owner'로 변경
// (setupEventListeners, setupAnimations, openEditModal 등은 동일)

// 정보 수정 처리 (점주용)
async function handleEditSubmit(e) {
    e.preventDefault();

    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;

    submitBtn.textContent = '저장 중...';
    submitBtn.disabled = true;

    try {
        const phone = document.getElementById('editPhone').value.trim();
        const updateData = { phone };

        const response = await authenticatedFetch('/api/owner', {
            method: 'PATCH',
            body: JSON.stringify(updateData)
        });

        if (!response.ok) {
            throw new Error('정보 수정에 실패했습니다');
        }

        showSuccessMessage('정보가 성공적으로 수정되었습니다.');
        closeModalWithAnimation();

        await loadOwnerProfile();

    } catch (error) {
        console.error('정보 수정 오류:', error);
        showErrorMessage('정보 수정에 실패했습니다. 다시 시도해주세요.');
    } finally {
        submitBtn.textContent = originalText;
        submitBtn.disabled = false;
    }
}

// 회원 탈퇴 (점주용)
async function deleteOwnerAccount() {
    const message = '정말로 회원 탈퇴하시겠습니까?\n\n⚠️ 탈퇴 후에는 세탁소 관리를 할 수 없습니다.';

    if (!confirm(message)) return;
    if (!confirm('마지막 확인입니다.\n정말로 탈퇴하시겠습니까?')) return;

    try {
        const response = await authenticatedFetch('/api/owner', {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error('회원 탈퇴에 실패했습니다');
        }

        showSuccessMessage('회원 탈퇴가 완료되었습니다.\n이용해 주셔서 감사합니다.');

        setTimeout(() => {
            removeToken();
            window.location.href = '/';
        }, 2000);

    } catch (error) {
        console.error('회원 탈퇴 오류:', error);
        showErrorMessage('회원 탈퇴에 실패했습니다. 다시 시도해주세요.');
    }
}

// 나머지 유틸리티 함수들은 customer-profile.js와 동일
