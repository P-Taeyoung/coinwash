// 고객 회원가입 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('customerSignupForm');

    // 폼 제출 이벤트
    form.addEventListener('submit', handleSignup);

    // 비밀번호 확인 검증
    const password = document.getElementById('password');
    const confirmPassword = document.getElementById('confirmPassword');

    confirmPassword.addEventListener('blur', function() {
        if (password.value !== confirmPassword.value) {
            confirmPassword.setCustomValidity('비밀번호가 일치하지 않습니다.');
        } else {
            confirmPassword.setCustomValidity('');
        }
    });

    // 회원가입 처리
    async function handleSignup(e) {
        e.preventDefault();

        const formData = new FormData(e.target);
        const data = Object.fromEntries(formData);

        // 유효성 검사
        if (!validateForm(data)) {
            return;
        }

        try {
            const response = await fetch('/api/customer/signup', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    id: data.id,
                    password: data.password,
                    name: data.name,
                    phone: data.phone,
                    address: data.address,
                    latitude: parseFloat(data.latitude),
                    longitude: parseFloat(data.longitude)
                })
            });

            if (response.ok) {
                alert('회원가입이 완료되었습니다. 로그인해주세요.');
                window.location.href = '/auth/signin';
            } else {
                const error = await response.text();
                alert(error || '회원가입에 실패했습니다.');
            }
        } catch (error) {
            console.error('회원가입 오류:', error);
            alert('회원가입에 실패했습니다.');
        }
    }

    // 폼 유효성 검사
    function validateForm(data) {
        // 아이디 검사
        if (!/^[a-zA-Z0-9]{4,20}$/.test(data.id)) {
            alert('아이디는 영문, 숫자 조합 4-20자여야 합니다.');
            return false;
        }

        // 비밀번호 검사
        if (!/^(?=.*[a-zA-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/.test(data.password)) {
            alert('비밀번호는 영문, 숫자, 특수문자 조합 8자 이상이어야 합니다.');
            return false;
        }

        // 비밀번호 확인
        if (data.password !== data.confirmPassword) {
            alert('비밀번호가 일치하지 않습니다.');
            return false;
        }

        // 전화번호 검사
        if (!/^010-\d{4}-\d{4}$/.test(data.phone)) {
            alert('전화번호 형식이 올바르지 않습니다. (010-1234-5678)');
            return false;
        }

        // ✅ 주소 선택 확인 (address-search-utils.js가 설정한 필드들 확인)
        if (!data.address || !data.latitude || !data.longitude) {
            alert('주소를 선택해주세요.');
            return false;
        }

        return true;
    }
});
