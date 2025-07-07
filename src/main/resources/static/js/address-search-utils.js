// 주소 검색 관련 변수
let currentPage = 1;
let totalPages = 1;
let currentQuery = '';
let selectedAddressData = null;

// 주소 검색 초기화 함수
function initAddressSearch() {
    setupAddressSearchListeners();
}

// 주소 검색 리스너 설정
function setupAddressSearchListeners() {
    const addressSearch = document.getElementById('addressSearch');
    const searchAddressBtn = document.getElementById('searchAddressBtn');
    const clearResults = document.querySelector('.clear-results-btn');
    const clearSelected = document.getElementById('clearSelected');

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
}

// 주소 검색 처리
async function handleAddressSearch() {
    const addressSearch = document.getElementById('addressSearch');
    const searchBtn = document.getElementById('searchAddressBtn');
    const addressResults = document.getElementById('addressResults');
    const addressList = document.getElementById('addressList');

    const query = addressSearch.value.trim();
    if (!query) {
        showNotification('검색할 주소를 입력해주세요.', 'error');
        return;
    }

    // 새로운 검색인 경우 페이지 초기화
    if (query !== currentQuery) {
        currentPage = 1;
        currentQuery = query;
    }

    // 로딩 상태
    searchBtn.disabled = true;
    searchBtn.textContent = '🔍 검색 중...';

    // 결과 영역 표시 및 로딩
    addressResults.style.display = 'block';
    addressList.innerHTML = '<li class="search-loading">주소를 검색하는 중...</li>';

    try {
        const response = await fetch(`/api/address?query=${encodeURIComponent(query)}&page=${currentPage}`, {
            headers: {
                'Authorization': `Bearer ${getToken()}`
            }
        });

        if (!response.ok) {
            throw new Error('주소 검색에 실패했습니다');
        }

        const data = await response.json();

        // 서버 응답 형태에 따라 처리
        const addresses = Array.isArray(data) ? data : (data.addresses || []);

        displayAddressResults(addresses);
        updatePagination(addresses.length);

    } catch (error) {
        console.error('주소 검색 오류:', error);
        addressList.innerHTML = `
            <li class="address-item error-item">
                <div class="address-name">⚠️ 검색 실패</div>
                <div class="address-coords">주소 검색에 실패했습니다. 다시 시도해주세요.</div>
            </li>
        `;
        showNotification('주소 검색에 실패했습니다.', 'error');
    } finally {
        searchBtn.disabled = false;
        searchBtn.textContent = '🔍 주소 검색';
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
        hidePagination();
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

    // 숨겨진 폼 필드들
    const addressNameField = document.getElementById('addressName');
    const latitudeField = document.getElementById('latitude');
    const longitudeField = document.getElementById('longitude');

    if (!selectedAddress) return;

    // 도로명 주소가 있으면 우선 표시, 없으면 지번 주소 표시
    const displayName = addressData.roadAddress || addressData.address;
    const subAddress = addressData.roadAddress ? addressData.address : '';

    // ✅ 표시 영역 업데이트 (customer와 동일)
    if (selectedAddressName) {
        selectedAddressName.textContent = displayName;
    }
    if (selectedAddressCoords) {
        selectedAddressCoords.textContent = subAddress ? `📍 ${subAddress}` : '';
    }
    selectedAddress.style.display = 'block';

    // ✅ 폼 필드에 값 설정 (owner용)
    if (addressNameField) {
        addressNameField.value = displayName;
    }
    if (latitudeField && addressData.latitude) {
        latitudeField.value = addressData.latitude;
    }
    if (longitudeField && addressData.longitude) {
        longitudeField.value = addressData.longitude;
    }

    console.log('✅ 주소 선택 완료:', {
        display: displayName,
        addressName: addressNameField?.value,
        latitude: latitudeField?.value,
        longitude: longitudeField?.value
    });
}

// 페이지네이션 업데이트 - profile.js 방식 적용
function updatePagination(addressCount) {
    const addressResults = document.getElementById('addressResults');

    // 기존 페이지네이션 제거
    const existingPagination = addressResults.querySelector('.pagination-container');
    if (existingPagination) {
        existingPagination.remove();
    }

    // 주소가 있고, 페이지네이션이 필요한 경우에만 추가
    if (addressCount > 0) {
        // profile.js와 동일한 방식으로 totalPages 계산
        totalPages = Math.max(1, currentPage + 1); // 임시로 설정

        const paginationHtml = `
            <div class="pagination-container">
                <button class="page-btn" id="prevPageBtn" ${currentPage <= 1 ? 'disabled' : ''}>이전</button>
                <span class="page-info" id="pageInfo">${currentPage} / ${totalPages}</span>
                <button class="page-btn" id="nextPageBtn" ${currentPage >= totalPages ? 'disabled' : ''}>다음</button>
            </div>
        `;

        addressResults.insertAdjacentHTML('beforeend', paginationHtml);

        // 페이지네이션 이벤트 리스너 추가 - profile.js 방식과 동일
        setupPaginationListeners();
    }
}

// 페이지네이션 리스너 설정 - profile.js 방식
function setupPaginationListeners() {
    const prevPageBtn = document.getElementById('prevPageBtn');
    const nextPageBtn = document.getElementById('nextPageBtn');

    if (prevPageBtn) {
        prevPageBtn.addEventListener('click', () => {
            console.log('이전 페이지 클릭, 현재 페이지:', currentPage);
            if (currentPage > 1) {
                currentPage--;
                handleAddressSearch();
            }
        });
    }

    if (nextPageBtn) {
        nextPageBtn.addEventListener('click', () => {
            console.log('다음 페이지 클릭, 현재 페이지:', currentPage);
            if (currentPage < totalPages) {
                currentPage++;
                handleAddressSearch();
            }
        });
    }
}

// 페이지네이션 숨기기
function hidePagination() {
    const addressResults = document.getElementById('addressResults');
    const existingPagination = addressResults.querySelector('.pagination-container');
    if (existingPagination) {
        existingPagination.remove();
    }
}

// 검색 결과 숨기기
function hideAddressResults() {
    const addressResults = document.getElementById('addressResults');
    if (addressResults) {
        addressResults.style.display = 'none';
    }
    hidePagination();
}

// 선택된 주소 지우기
function clearSelectedAddress() {
    const selectedAddress = document.getElementById('selectedAddress');
    const addressNameField = document.getElementById('addressName');
    const latitudeField = document.getElementById('latitude');
    const longitudeField = document.getElementById('longitude');

    if (selectedAddress) {
        selectedAddress.style.display = 'none';
    }

    // 폼 필드들도 초기화
    if (addressNameField) addressNameField.value = '';
    if (latitudeField) latitudeField.value = '';
    if (longitudeField) longitudeField.value = '';

    selectedAddressData = null;

    // 선택 상태 제거
    document.querySelectorAll('.address-item.selected').forEach(el => {
        el.classList.remove('selected');
    });

    console.log('🗑️ 주소 선택 해제됨');
}

// 선택된 주소 데이터 반환
function getSelectedAddress() {
    return selectedAddressData;
}

// 토큰 가져오기 함수 (없으면 추가)
function getToken() {
    return localStorage.getItem('token') || sessionStorage.getItem('token');
}

// 알림 표시 함수 (없으면 추가)
function showNotification(message, type) {
    // 기존 알림이 있으면 제거
    const existingNotification = document.querySelector('.notification-toast');
    if (existingNotification) {
        existingNotification.remove();
    }

    const notification = document.createElement('div');
    notification.className = 'notification-toast';
    notification.style.cssText = `
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
        notification.style.background = 'linear-gradient(135deg, #27ae60, #2ecc71)';
        notification.innerHTML = `✅ ${message}`;
    } else {
        notification.style.background = 'linear-gradient(135deg, #e74c3c, #c0392b)';
        notification.innerHTML = `❌ ${message}`;
    }

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.remove();
    }, 3000);
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    initAddressSearch();
});

