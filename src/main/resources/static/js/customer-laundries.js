document.addEventListener('DOMContentLoaded', function() {
    let currentLatitude = null;
    let currentLongitude = null;

    // DOM 요소들
    const getCurrentLocationBtn = document.getElementById('getCurrentLocationBtn');
    const distanceSelect = document.getElementById('distanceSelect');
    const searchLaundriesBtn = document.getElementById('searchLaundriesBtn');
    const laundriesList = document.getElementById('laundriesList');
    const loading = document.getElementById('loading');

    // 🔧 search-address.js의 선택된 주소 모니터링
    function checkSelectedAddress() {
        const selectedAddress = getSelectedAddress(); // search-address.js의 함수

        if (selectedAddress && selectedAddress.latitude && selectedAddress.longitude) {
            currentLatitude = selectedAddress.latitude;
            currentLongitude = selectedAddress.longitude;
            console.log('✅ 주소에서 좌표 가져옴:', {
                address: selectedAddress.address || selectedAddress.roadAddress,
                currentLatitude,
                currentLongitude
            });
            return true;
        } else {
            console.log('❌ 선택된 주소에 좌표 정보 없음:', selectedAddress);
            return false;
        }
    }

    // 🔧 주소 선택 감지를 위한 MutationObserver 사용
    function observeAddressSelection() {
        const selectedAddressElement = document.getElementById('selectedAddress');
        if (selectedAddressElement) {
            const observer = new MutationObserver(function(mutations) {
                mutations.forEach(function(mutation) {
                    if (mutation.type === 'attributes' && mutation.attributeName === 'style') {
                        // 선택된 주소가 표시되면 좌표 업데이트
                        if (selectedAddressElement.style.display !== 'none') {
                            setTimeout(() => {
                                checkSelectedAddress();
                            }, 100); // 약간의 지연을 두어 DOM 업데이트 완료 대기
                        } else {
                            // 주소가 지워지면 좌표도 초기화
                            currentLatitude = null;
                            currentLongitude = null;
                            console.log('🗑️ 주소 선택 해제됨');
                        }
                    }
                });
            });

            observer.observe(selectedAddressElement, {
                attributes: true,
                attributeFilter: ['style']
            });

            console.log('👀 주소 선택 감지 시작됨');
        } else {
            console.warn('⚠️ selectedAddress 요소를 찾을 수 없습니다');
        }
    }

    // 🔧 페이지 로드 시 주소 선택 감지 시작
    setTimeout(() => {
        observeAddressSelection();
        // 이미 선택된 주소가 있는지 확인
        checkSelectedAddress();
    }, 500);

    // 이벤트 리스너
    getCurrentLocationBtn.addEventListener('click', getCurrentLocation);
    searchLaundriesBtn.addEventListener('click', searchLaundries);

    // 현재 위치 가져오기
    function getCurrentLocation() {
        if (!navigator.geolocation) {
            alert('이 브라우저에서는 위치 서비스를 지원하지 않습니다.');
            return;
        }

        showLoading();
        navigator.geolocation.getCurrentPosition(
            function(position) {
                currentLatitude = position.coords.latitude;
                currentLongitude = position.coords.longitude;

                // 🔧 search-address.js의 함수 사용해서 현재 위치 표시
                const currentLocationData = {
                    address: `현재 위치`,
                    roadAddress: `위도: ${currentLatitude.toFixed(6)}, 경도: ${currentLongitude.toFixed(6)}`,
                    latitude: currentLatitude,
                    longitude: currentLongitude
                };

                // search-address.js의 전역 변수와 함수 사용
                if (typeof selectedAddressData !== 'undefined' && typeof displaySelectedAddress === 'function') {
                    selectedAddressData = currentLocationData;
                    displaySelectedAddress(currentLocationData);

                    // 검색 결과가 열려있다면 닫기
                    if (typeof hideAddressResults === 'function') {
                        hideAddressResults();
                    }
                }

                hideLoading();
                console.log('✅ 현재 위치 설정 완료:', { currentLatitude, currentLongitude });
            },
            function(error) {
                hideLoading();
                console.error('위치 가져오기 실패:', error);

                let errorMessage = '현재 위치를 가져올 수 없습니다.';
                switch(error.code) {
                    case error.PERMISSION_DENIED:
                        errorMessage = '위치 권한이 거부되었습니다. 브라우저 설정에서 위치 권한을 허용해주세요.';
                        break;
                    case error.POSITION_UNAVAILABLE:
                        errorMessage = '위치 정보를 사용할 수 없습니다.';
                        break;
                    case error.TIMEOUT:
                        errorMessage = '위치 요청 시간이 초과되었습니다.';
                        break;
                }

                alert(errorMessage + ' 주소를 직접 검색해주세요.');
            }
        );
    }

    // 세탁소 검색
    async function searchLaundries() {
        // 🔧 최신 주소 정보 다시 확인
        const hasValidCoords = checkSelectedAddress();

        if (!hasValidCoords || !currentLatitude || !currentLongitude) {
            console.log('❌ 좌표 정보 부족:', {
                hasValidCoords,
                currentLatitude,
                currentLongitude,
                selectedAddress: getSelectedAddress()
            });

            alert('먼저 위치를 설정해주세요.\n\n1. "현재 위치 가져오기" 버튼을 클릭하거나\n2. 위의 주소 검색에서 주소를 선택해주세요.');
            return;
        }

        console.log('🎯 세탁소 검색 시작:', {
            currentLatitude,
            currentLongitude,
            distance: distanceSelect.value + 'km'
        });

        const distance = parseFloat(distanceSelect.value) * 1000; // 미터 단위로 변환

        try {
            showLoading();
            const response = await fetch(
                `/api/laundries?longitude=${currentLongitude}&latitude=${currentLatitude}&distance=${distance}`,
                {
                    headers: {
                        'Authorization': `Bearer ${getToken()}`
                    }
                }
            );

            if (response.ok) {
                const laundries = await response.json();
                console.log('✅ 세탁소 검색 결과:', laundries);
                displayLaundries(laundries);
            } else {
                const errorText = await response.text();
                console.error('세탁소 검색 실패:', response.status, errorText);
                throw new Error(`세탁소 검색에 실패했습니다. (${response.status})`);
            }
        } catch (error) {
            console.error('세탁소 검색 오류:', error);
            alert('세탁소 검색에 실패했습니다. 잠시 후 다시 시도해주세요.');
        } finally {
            hideLoading();
        }
    }

    // 세탁소 목록 표시
    function displayLaundries(laundries) {
        if (!laundries || laundries.length === 0) {
            laundriesList.innerHTML = `
                <div class="empty-state">
                    <div class="empty-icon">🏪</div>
                    <h3>주변에 세탁소가 없습니다</h3>
                    <p>검색 거리를 늘려서 다시 검색해보세요</p>
                    <button class="btn btn-outline" onclick="document.getElementById('distanceSelect').value='5'; searchLaundries();">
                        🔍 5km로 재검색
                    </button>
                </div>
            `;
            return;
        }

        laundriesList.innerHTML = laundries.map(laundry => `
            <div class="laundry-card" data-laundry-id="${laundry.laundryId}">
                <div class="laundry-header">
                    <h3 class="laundry-name">
                        🏪 ${laundry.name || '세탁소'}
                        ${laundry.distance ? `<span class="laundry-distance">${formatDistance(laundry.distance)}</span>` : ''}
                    </h3>
                    <div class="laundry-status ${laundry.opened ? 'open' : 'closed'}">
                        ${laundry.opened ? '✅ 영업중' : '❌ 영업종료'}
                    </div>
                </div>
                
                <div class="laundry-info">
                    <p class="laundry-address">📍 ${laundry.addressName || laundry.address}</p>
                    ${laundry.description ? `<p class="laundry-description">💬 ${laundry.description}</p>` : ''}
                    ${laundry.phoneNumber ? `<p class="laundry-phone">📞 ${laundry.phoneNumber}</p>` : ''}
                </div>
                
                <div class="laundry-actions">
                    <button class="btn btn-primary" onclick="viewMachines(${laundry.laundryId})" ${!laundry.opened ? 'disabled' : ''}>
                        🔧 기계 현황 보기
                    </button>
                    ${laundry.phoneNumber ? `
                        <button class="btn btn-outline" onclick="window.open('tel:${laundry.phoneNumber}')">
                            📞 전화하기
                        </button>
                    ` : ''}
                </div>
            </div>
        `).join('');

        console.log(`✅ ${laundries.length}개 세탁소 표시 완료`);
    }

    // 거리 포맷팅 함수
    function formatDistance(distance) {
        if (distance < 1000) {
            return `${Math.round(distance)}m`;
        } else {
            return `${(distance / 1000).toFixed(1)}km`;
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

    // 🔧 전역 함수로 노출 (디버깅용)
    window.debugLaundrySearch = {
        checkSelectedAddress,
        getCurrentCoords: () => ({ currentLatitude, currentLongitude }),
        getSelectedAddress: () => typeof getSelectedAddress === 'function' ? getSelectedAddress() : null
    };
});

// 기계 현황 보기
function viewMachines(laundryId) {
    console.log('🔧 기계 현황 보기:', laundryId);
    window.location.href = `/customer/machines?laundryId=${laundryId}`;
}

// 🔧 토큰 가져오기 함수
function getToken() {
    return localStorage.getItem('token') || sessionStorage.getItem('token');
}
