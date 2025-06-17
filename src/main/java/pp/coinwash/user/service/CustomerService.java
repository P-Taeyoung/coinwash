package pp.coinwash.user.service;

import static pp.coinwash.common.exception.ErrorCode.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pp.coinwash.common.exception.CustomException;
import pp.coinwash.common.exception.ErrorCode;
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

		return jwtProvider.generateToken(UserAuthDto.fromCustomer(validateId(dto)));
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

	private Customer validateId(UserSignInDto dto) {
		Customer customer =  customerRepository.findByLoginIdAndDeletedAtIsNull(dto.signInId())
			.orElseThrow(() -> new CustomException(INVALID_CREDENTIALS));

		if (!passwordEncoder.matches(dto.password(), customer.getPassword())) {
			throw new CustomException(INVALID_CREDENTIALS);
		}

		return customer;
	}

	private void existsId(String id) {
		if (customerRepository.existsByLoginIdAndDeletedAtIsNull(id)) {
			throw new CustomException(ALREADY_EXISTS_ID);
		}
	}

	private Customer validateCustomer(long customerId) {
		return customerRepository.findByCustomerIdAndDeletedAtIsNull(customerId)
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND));
	}
}
