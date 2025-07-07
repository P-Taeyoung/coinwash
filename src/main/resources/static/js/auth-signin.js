// 로그인 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('signinForm');
    const userTypeBtns = document.querySelectorAll('.user-type-btn');
    const customerSignupLink = document.getElementById('customerSignupLink');
    const ownerSignupLink = document.getElementById('ownerSignupLink');

    let currentUserType = 'customer';

    // 사용자 유형 선택 이벤트
    userTypeBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            // 모든 버튼에서 active 클래스 제거
            userTypeBtns.forEach(b => b.classList.remove('active'));

            // 클릭된 버튼에 active 클래스 추가
            this.classList.add('active');

            // 현재 사용자 유형 설정
            currentUserType = this.dataset.type;

            // 회원가입 링크 변경
            if (currentUserType === 'customer') {
                customerSignupLink.style.display = 'inline';
                ownerSignupLink.style.display = 'none';
            } else {
                customerSignupLink.style.display = 'none';
                ownerSignupLink.style.display = 'inline';
            }
        });
    });

    // 폼 제출 이벤트
    form.addEventListener('submit', handleSignin);

    // 로그인 처리
    async function handleSignin(e) {
        e.preventDefault();
        console.log('🔧 로그인 시작');

        const formData = new FormData(e.target);
        const data = Object.fromEntries(formData);

        // 유효성 검사
        if (!data.signInId || !data.password) {
            alert('아이디와 비밀번호를 입력해주세요.');
            return;
        }

        console.log('🔧 로그인 데이터:', { signInId: data.signInId, userType: currentUserType });

        try {
            const endpoint = currentUserType === 'customer' ? '/api/customer/signin' : '/api/owner/signin';
            console.log('🔧 요청 엔드포인트:', endpoint);

            const response = await fetch(endpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    signInId: data.signInId,
                    password: data.password
                })
            });

            console.log('🔧 응답 상태:', response.status, response.ok);

            if (response.ok) {
                const token = await response.text();
                console.log('🔧 토큰 받음:', token.substring(0, 20) + '...');

                const rememberMe = data.rememberMe === 'on' || true;
                console.log('🔧 rememberMe:', rememberMe);

                // 🔧 auth-signin.js에서 직접 토큰 저장
                setTokenInSignin(token, rememberMe);

                // 헤더 업데이트
                if (typeof window.updateHeader === 'function') {
                    console.log('🔧 헤더 업데이트 호출');
                    window.updateHeader();
                }

                alert('로그인되었습니다.');

                // 사용자 유형에 따른 리디렉션
                if (currentUserType === 'customer') {
                    window.location.href = '/customer/laundries';
                } else {
                    window.location.href = '/owner/laundries';
                }
            } else {
                const error = await response.text();
                console.log('❌ 로그인 실패:', error);
                alert(error || '로그인에 실패했습니다.');
            }
        } catch (error) {
            console.error('❌ 로그인 오류:', error);
            alert('로그인에 실패했습니다.');
        }
    }

    // 🔧 auth-signin.js 전용 토큰 설정 함수
    function setTokenInSignin(token, remember = false) {
        const cleanToken = token.startsWith('Bearer ') ? token.substring(7) : token;

        console.log('🔧 auth-signin.js에서 토큰 설정:', {
            token: token.substring(0, 20) + '...',
            remember
        });

        // 1. localStorage/sessionStorage 설정
        if (remember) {
            localStorage.setItem('token', cleanToken);
            sessionStorage.removeItem('token');
        } else {
            sessionStorage.setItem('token', cleanToken);
            localStorage.removeItem('token');
        }

        // 2. 🍪 쿠키 설정 (SSE용)
        const isHttps = location.protocol === 'https:';
        const secureAttr = isHttps ? 'Secure;' : '';
        const maxAge = remember ? 'max-age=604800;' : ''; // 7일 또는 세션

        const cookieString = `token=Bearer ${cleanToken}; path=/; ${maxAge} SameSite=Lax; ${secureAttr}`;

        console.log('🍪 auth-signin.js 쿠키 설정:', cookieString);
        document.cookie = cookieString;

        // 3. 설정 후 확인
        setTimeout(() => {
            console.log('🍪 auth-signin.js 설정 후 쿠키:', document.cookie);
            console.log('💾 localStorage:', localStorage.getItem('token'));
            console.log('💾 sessionStorage:', sessionStorage.getItem('token'));
        }, 100);

        // 4. 🔔 커스텀 이벤트 발생 (SSE 연결을 위해)
        window.dispatchEvent(new CustomEvent('tokenChanged', {
            detail: { action: 'set', token: cleanToken }
        }));
    }
});

// 🔧 쿠키 값 읽기 함수
function getCookieValue(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return null;
}
