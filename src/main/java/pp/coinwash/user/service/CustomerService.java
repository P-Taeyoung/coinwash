package pp.coinwash.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pp.coinwash.user.domain.dto.CustomerResponseDto;
import pp.coinwash.user.domain.dto.CustomerSignUpDto;
import pp.coinwash.user.domain.dto.CustomerUpdateDto;
import pp.coinwash.user.domain.entity.Customer;
import pp.coinwash.user.domain.repository.CustomerRepository;

@Service
@RequiredArgsConstructor
public class CustomerService {

	private final CustomerRepository customerRepository;

	public void signUp(CustomerSignUpDto dto) {
		validateId(dto.id());
		customerRepository.save(Customer.of(dto));
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

	private void validateId(String Id) {
		if (customerRepository.existsByLoginId(Id)) {
			throw new RuntimeException("이미 존재하는 아이디입니다.");
		}
	}

	private Customer validateCustomer(long customerId) {
		return customerRepository.findById(customerId)
			.orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));
	}
}
