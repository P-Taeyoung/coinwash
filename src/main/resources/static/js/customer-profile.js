// 고객 프로필 페이지 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    loadCustomerProfile();
    setupEventListeners();
    setupAnimations();
});

let currentUserData = null;

// 고객 프로필 정보 로드
async function loadCustomerProfile() {
    const loadingState = document.getElementById('loadingState');
    const errorState = document.getElementById('errorState');
    const customerProfile = document.getElementById('customerProfile');

    try {
        const response = await authenticatedFetch('/api/customer');

        if (!response.ok) {
            throw new Error('고객 정보를 불러올 수 없습니다');
        }

        const userData = await response.json();
        currentUserData = userData;

        // UI 업데이트
        displayCustomerProfile(userData);

        loadingState.style.display = 'none';
        errorState.style.display = 'none';
        customerProfile.style.display = 'block';

        // 애니메이션 시작
        triggerAnimations();

    } catch (error) {
        console.error('프로필 로드 오류:', error);
        loadingState.style.display = 'none';
        errorState.style.display = 'block';
    }
}

// 고객 프로필 표시
function displayCustomerProfile(data) {
    document.getElementById('customerId').textContent = data.id || '-';
    document.getElementById('customerName').textContent = data.name || '-';
    document.getElementById('customerPhone').textContent = data.phone || '-';
    document.getElementById('customerAddress').textContent = data.address || '-';

    // 포인트 애니메이션과 함께 표시
    animatePointCounter(data.points || 0);
}

// 포인트 카운터 애니메이션
function animatePointCounter(targetPoints) {
    const pointElement = document.getElementById('customerPoints');
    const duration = 1500;
    const startTime = performance.now();

    function updateCounter(currentTime) {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);

        // easeOutQuart 이징 함수
        const easeProgress = 1 - Math.pow(1 - progress, 4);
        const currentPoints = Math.floor(targetPoints * easeProgress);

        pointElement.textContent = currentPoints.toLocaleString();

        if (progress < 1) {
            requestAnimationFrame(updateCounter);
        }
    }

    requestAnimationFrame(updateCounter);
}

// 이벤트 리스너 설정
function setupEventListeners() {
    // 정보 수정 버튼
    const editBasicBtn = document.getElementById('editBasicBtn');
    if (editBasicBtn) {
        editBasicBtn.addEventListener('click', openEditModal);
    }

    // 회원 탈퇴 버튼
    const deleteAccountBtn = document.getElementById('deleteAccountBtn');
    if (deleteAccountBtn) {
        deleteAccountBtn.addEventListener('click', deleteCustomerAccount);
    }



    // 모달 관련
    setupModalListeners();
    // 주소 검색 리스너들 추가
    setupAddressSearchListeners();
}

// 주소 검색 관련 변수
let currentPage = 1;
let totalPages = 1;
let currentQuery = '';
let selectedAddressData = null;

// 주소 검색 리스너 설정
function setupAddressSearchListeners() {
    const addressSearch = document.getElementById('addressSearch');
    const searchAddressBtn = document.getElementById('searchAddressBtn');
    const clearResults = document.getElementById('clearResults');
    const clearSelected = document.getElementById('clearSelected');
    const prevPage = document.getElementById('prevPage');
    const nextPage = document.getElementById('nextPage');

    // 검색 버튼 클릭
    if (searchAddressBtn) {
        searchAddressBtn.addEventListener('click', handleAddressSearch);
    }

    // 엔터키로 검색
    if (addressSearch) {
        addressSearch.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                handleAddressSearch();
            }
        });

        // 실시간 검색 (디바운스 적용)
        let searchTimeout;
        addressSearch.addEventListener('input', (e) => {
            clearTimeout(searchTimeout);
            const query = e.target.value.trim();

            if (query.length >= 2) {
                searchTimeout = setTimeout(() => {
                    handleAddressSearch();
                }, 500);
            } else if (query.length === 0) {
                hideAddressResults();
            }
        });
    }

    // 결과 지우기
    if (clearResults) {
        clearResults.addEventListener('click', hideAddressResults);
    }

    // 선택된 주소 지우기
    if (clearSelected) {
        clearSelected.addEventListener('click', clearSelectedAddress);
    }

    // 페이지네이션
    if (prevPage) {
        prevPage.addEventListener('click', () => {
            if (currentPage > 1) {
                currentPage--;
                handleAddressSearch();
            }
        });
    }

    if (nextPage) {
        nextPage.addEventListener('click', () => {
            if (currentPage < totalPages) {
                currentPage++;
                handleAddressSearch();
            }
        });
    }
}

// 주소 검색 처리
async function handleAddressSearch() {
    const addressSearch = document.getElementById('addressSearch');
    const searchBtn = document.getElementById('searchAddressBtn');
    const addressResults = document.getElementById('addressResults');
    const addressList = document.getElementById('addressList');

    const query = addressSearch.value.trim();
    if (!query) {
        showErrorMessage('검색할 주소를 입력해주세요.');
        return;
    }

    // 새로운 검색인 경우 페이지 초기화
    if (query !== currentQuery) {
        currentPage = 1;
        currentQuery = query;
    }

    // 로딩 상태
    searchBtn.disabled = true;
    searchBtn.textContent = '검색 중...';

    // 결과 영역 표시 및 로딩
    addressResults.style.display = 'block';
    addressList.innerHTML = '<li class="search-loading">주소를 검색하는 중...</li>';

    try {
        const response = await fetch(`/api/address?query=${encodeURIComponent(query)}&page=${currentPage}`);

        if (!response.ok) {
            throw new Error('주소 검색에 실패했습니다');
        }

        const addresses = await response.json();
        displayAddressResults(addresses);
        updatePagination();

    } catch (error) {
        console.error('주소 검색 오류:', error);
        addressList.innerHTML = `
            <li class="address-item error-item">
                <div class="address-name">⚠️ 검색 실패</div>
                <div class="address-coords">주소 검색에 실패했습니다. 다시 시도해주세요.</div>
            </li>
        `;
        showErrorMessage('주소 검색에 실패했습니다.');
    } finally {
        searchBtn.disabled = false;
        searchBtn.textContent = '🔍 검색';
    }
}

// 주소 검색 결과 표시
function displayAddressResults(addresses) {
    const addressList = document.getElementById('addressList');

    if (!addresses || addresses.length === 0) {
        addressList.innerHTML = `
            <li class="address-item no-results">
                <div class="address-name">🔍 검색 결과 없음</div>
                <div class="address-coords">다른 키워드로 검색해보세요.</div>
            </li>
        `;
        return;
    }

    addressList.innerHTML = addresses.map((address, index) => `
        <li class="address-item" data-index="${index}" data-address='${JSON.stringify(address)}'>
            <div class="address-name">${address.roadAddress || address.address}</div>
            <div class="address-coords">📍 ${address.address}</div>
        </li>
    `).join('');

    // 주소 선택 이벤트 추가
    addressList.querySelectorAll('.address-item').forEach(item => {
        if (!item.classList.contains('no-results') && !item.classList.contains('error-item')) {
            item.addEventListener('click', () => selectAddress(item));
        }
    });
}

// 주소 선택
function selectAddress(item) {
    const addressData = JSON.parse(item.dataset.address);
    selectedAddressData = addressData;

    // 기존 선택 제거
    document.querySelectorAll('.address-item.selected').forEach(el => {
        el.classList.remove('selected');
    });

    // 새로운 선택 표시
    item.classList.add('selected');

    // 선택된 주소 표시
    displaySelectedAddress(addressData);

    // 폼 필드 업데이트
    updateAddressFields(addressData);

    // 검색 결과 숨기기 (선택 후)
    setTimeout(() => {
        hideAddressResults();
    }, 1000);
}

// 선택된 주소 표시
function displaySelectedAddress(addressData) {
    const selectedAddress = document.getElementById('selectedAddress');
    const selectedAddressName = document.getElementById('selectedAddressName');
    const selectedAddressCoords = document.getElementById('selectedAddressCoords');

    // 도로명 주소가 있으면 우선 표시, 없으면 지번 주소 표시
    const displayName = addressData.roadAddress || addressData.address;
    const subAddress = addressData.roadAddress ? addressData.address : '';

    selectedAddressName.textContent = displayName;
    selectedAddressCoords.textContent = subAddress ? `📍 ${subAddress}` : '';

    selectedAddress.style.display = 'block';
}

// 주소 필드 업데이트
function updateAddressFields(addressData) {
    // 도로명 주소를 우선으로, 없으면 지번 주소 사용
    const addressToSave = addressData.roadAddress || addressData.address;

    document.getElementById('editAddress').value = addressToSave;
    document.getElementById('editLatitude').value = addressData.latitude;
    document.getElementById('editLongitude').value = addressData.longitude;
}

// 페이지네이션 업데이트
function updatePagination() {
    const prevPage = document.getElementById('prevPage');
    const nextPage = document.getElementById('nextPage');
    const pageInfo = document.getElementById('pageInfo');

    // 실제로는 API 응답에서 총 페이지 수를 받아와야 하지만,
    // 여기서는 간단히 처리
    totalPages = Math.max(1, currentPage + 1); // 임시로 설정

    prevPage.disabled = currentPage <= 1;
    nextPage.disabled = currentPage >= totalPages;
    pageInfo.textContent = `${currentPage} / ${totalPages}`;
}

// 검색 결과 숨기기
function hideAddressResults() {
    const addressResults = document.getElementById('addressResults');
    addressResults.style.display = 'none';
}

// 선택된 주소 지우기
function clearSelectedAddress() {
    const selectedAddress = document.getElementById('selectedAddress');
    selectedAddress.style.display = 'none';

    selectedAddressData = null;

    // 폼 필드 초기화
    document.getElementById('editAddress').value = '';
    document.getElementById('editLatitude').value = '';
    document.getElementById('editLongitude').value = '';

    // 선택 상태 제거
    document.querySelectorAll('.address-item.selected').forEach(el => {
        el.classList.remove('selected');
    });
}

// 애니메이션 설정
function setupAnimations() {
    // Intersection Observer로 스크롤 애니메이션
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);

    // 애니메이션 대상 요소들
    const animateElements = document.querySelectorAll('.info-card, .quick-menu-card, .point-card');
    animateElements.forEach(el => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(30px)';
        el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        observer.observe(el);
    });
}

// 애니메이션 트리거
function triggerAnimations() {
    // 카드들에 순차적 애니메이션 적용
    const cards = document.querySelectorAll('.info-card');
    cards.forEach((card, index) => {
        setTimeout(() => {
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, index * 100);
    });
}

// 수정된 openEditModal 함수
function openEditModal() {
    const modal = document.getElementById('editModal');
    const editPhone = document.getElementById('editPhone');
    const addressSearch = document.getElementById('addressSearch');

    editPhone.value = currentUserData.phone || '';

    // 기존 주소가 있으면 검색창에 표시
    if (currentUserData.address) {
        addressSearch.value = currentUserData.address;
        // 기존 주소 정보로 선택된 주소 표시
        if (currentUserData.latitude && currentUserData.longitude) {
            const existingAddress = {
                address: currentUserData.address,
                roadAddress: currentUserData.address, // 기존 저장된 주소를 도로명으로 가정
                latitude: currentUserData.latitude,
                longitude: currentUserData.longitude
            };
            displaySelectedAddress(existingAddress);
            updateAddressFields(existingAddress);
            selectedAddressData = existingAddress;
        }
    }

    modal.style.display = 'flex';

    setTimeout(() => {
        modal.querySelector('.modal-content').style.transform = 'translateY(0) scale(1)';
    }, 10);
}

// 모달 리스너 설정
function setupModalListeners() {
    const modal = document.getElementById('editModal');
    const closeModal = document.getElementById('closeModal');
    const cancelEdit = document.getElementById('cancelEdit');
    const editForm = document.getElementById('editForm');
    const modalOverlay = document.querySelector('.modal-overlay');

    // 모달 닫기
    [closeModal, cancelEdit, modalOverlay].forEach(element => {
        if (element) {
            element.addEventListener('click', closeModalWithAnimation);
        }
    });

    // ESC 키로 모달 닫기
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && modal.style.display === 'flex') {
            closeModalWithAnimation();
        }
    });

    // 폼 제출
    if (editForm) {
        editForm.addEventListener('submit', handleEditSubmit);
    }
}

// 모달 닫기 애니메이션
function closeModalWithAnimation() {
    const modal = document.getElementById('editModal');
    const modalContent = modal.querySelector('.modal-content');

    modalContent.style.transform = 'translateY(-50px) scale(0.9)';
    modalContent.style.opacity = '0';

    setTimeout(() => {
        modal.style.display = 'none';
        modalContent.style.transform = '';
        modalContent.style.opacity = '';

        // 주소 검색 관련 초기화
        hideAddressResults();
        clearSelectedAddress();
        document.getElementById('addressSearch').value = '';
        currentQuery = '';
        currentPage = 1;
    }, 300);
}

// 정보 수정 처리
// 수정된 handleEditSubmit 함수
async function handleEditSubmit(e) {
    e.preventDefault();

    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;

    submitBtn.textContent = '저장 중...';
    submitBtn.disabled = true;

    try {
        const phone = document.getElementById('editPhone').value.trim();
        const address = document.getElementById('editAddress').value.trim();
        const latitude = document.getElementById('editLatitude').value;
        const longitude = document.getElementById('editLongitude').value;

        // 주소가 선택되지 않은 경우 체크
        if (!address) {
            showErrorMessage('주소를 검색하고 선택해주세요.');
            return;
        }

        const updateData = {
            phone,
            address,
            latitude: latitude ? parseFloat(latitude) : null,
            longitude: longitude ? parseFloat(longitude) : null
        };

        const response = await authenticatedFetch('/api/customer', {
            method: 'PATCH',
            body: JSON.stringify(updateData)
        });

        if (!response.ok) {
            throw new Error('정보 수정에 실패했습니다');
        }

        showSuccessMessage('정보가 성공적으로 수정되었습니다.');
        closeModalWithAnimation();

        // 프로필 다시 로드
        await loadCustomerProfile();

    } catch (error) {
        console.error('정보 수정 오류:', error);
        showErrorMessage('정보 수정에 실패했습니다. 다시 시도해주세요.');
    } finally {
        submitBtn.textContent = originalText;
        submitBtn.disabled = false;
    }
}


// 회원 탈퇴
async function deleteCustomerAccount() {
    const message = '정말로 회원 탈퇴하시겠습니까?\n\n⚠️ 탈퇴 후에는 모든 정보가 삭제되며 복구할 수 없습니다.';

    if (!confirm(message)) {
        return;
    }

    // 재확인
    const confirmMessage = '마지막 확인입니다.\n정말로 탈퇴하시겠습니까?';
    if (!confirm(confirmMessage)) {
        return;
    }

    try {
        const response = await authenticatedFetch('/api/customer', {
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

// 성공 메시지 표시
function showSuccessMessage(message) {
    const toast = createToast(message, 'success');
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.remove();
    }, 3000);
}

// 에러 메시지 표시
function showErrorMessage(message) {
    const toast = createToast(message, 'error');
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.remove();
    }, 3000);
}

// 토스트 메시지 생성
function createToast(message, type) {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 10px;
        color: white;
        font-weight: 500;
        z-index: 10000;
        animation: slideInRight 0.3s ease;
        max-width: 300px;
        box-shadow: 0 5px 15px rgba(0,0,0,0.2);
    `;

    if (type === 'success') {
        toast.style.background = 'linear-gradient(135deg, #27ae60, #2ecc71)';
        toast.innerHTML = `✅ ${message}`;
    } else {
        toast.style.background = 'linear-gradient(135deg, #e74c3c, #c0392b)';
        toast.innerHTML = `❌ ${message}`;
    }

    return toast;
}

// 유틸리티 함수들
function getToken() {
    return localStorage.getItem('token') || sessionStorage.getItem('token');
}

function removeToken() {
    localStorage.removeItem('token');
    sessionStorage.removeItem('token');
}

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

    const response = await fetch(url, mergedOptions);

    if (response.status === 401) {
        removeToken();
        showErrorMessage('로그인이 만료되었습니다. 다시 로그인해주세요.');
        setTimeout(() => {
            window.location.href = '/auth/signin';
        }, 1500);
        return;
    }

    return response;
}

// CSS 애니메이션 추가
const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
`;
document.head.appendChild(style);
