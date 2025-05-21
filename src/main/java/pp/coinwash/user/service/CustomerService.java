package pp.coinwash.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pp.coinwash.security.JwtProvider;
import pp.coinwash.security.dto.UserAuthDto;
import pp.coinwash.user.domain.dto.CustomerResponseDto;
import pp.coinwash.user.domain.dto.UserSignInDto;
import pp.coinwash.user.domain.dto.CustomerSignUpDto;
import pp.coinwash.user.domain.dto.CustomerUpdateDto;
import pp.coinwash.user.domain.entity.Customer;
import pp.coinwash.user.domain.repository.CustomerRepository;

@Service
@RequiredArgsConstructor
public class CustomerService {

	private final CustomerRepository customerRepository;
	private final JwtProvider jwtProvider;
	private final PasswordEncoder passwordEncoder;

	public void signUp(CustomerSignUpDto dto) {
		existsId(dto.id());
		customerRepository.save(Customer.of(dto, passwordEncoder));
	}

	public String signIn(UserSignInDto dto) {
		Customer customer = validateId(dto.signInId());

		if (!passwordEncoder.matches(dto.password(), customer.getPassword())) {
			throw new RuntimeException("비밀번호가 일치하지 않습니다.");
		}

		return jwtProvider.generateToken(UserAuthDto.fromCustomer(customer));
	}

	public CustomerResponseDto getCustomer(long customerId) {
		return CustomerResponseDto.from(validateCustomer(customerId));
	}

	@Transactional
	public void updateCustomer(long customerId, CustomerUpdateDto dto) {
		validateCustomer(customerId).update(dto);
	}

	@Transactional
	public void deleteCustomer(long customerId) {
		validateCustomer(customerId).delete();
	}

	private Customer validateId(String id) {
		return customerRepository.findByLoginIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new RuntimeException("아이디 혹은 비밀번호가 일치하지 않습니다."));
	}

	private void existsId(String id) {
		if (customerRepository.existsByLoginIdAndDeletedAtIsNull(id)) {
			throw new RuntimeException("이미 존재하는 아이디입니다.");
		}
	}

	private Customer validateCustomer(long customerId) {
		return customerRepository.findByCustomerIdAndDeletedAtIsNull(customerId)
			.orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));
	}
}
