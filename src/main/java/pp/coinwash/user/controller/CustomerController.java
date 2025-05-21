package pp.coinwash.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pp.coinwash.user.domain.dto.CustomerResponseDto;
import pp.coinwash.user.domain.dto.CustomerSignInDto;
import pp.coinwash.user.domain.dto.CustomerSignUpDto;
import pp.coinwash.user.domain.dto.CustomerUpdateDto;
import pp.coinwash.user.service.CustomerService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customer")
public class CustomerController {

	private final CustomerService customerService;

	@PostMapping("/signin")
	public ResponseEntity<String> signIn(@RequestBody CustomerSignInDto dto) {

		return ResponseEntity.ok(customerService.signIn(dto));
	}

	@PostMapping("/signup")
	public ResponseEntity<String> signUpCustomer(@RequestBody CustomerSignUpDto dto) {
		customerService.signUp(dto);
		return ResponseEntity.ok("회원가입이 완료되었습니다.");
	}

	@GetMapping
	//TODO 추후 ContextHolder 에서 고객id 를 가져올 수 있도록
	public ResponseEntity<CustomerResponseDto> getCustomers(@RequestParam long customerId) {
		return ResponseEntity.ok(customerService.getCustomer(customerId));
	}

	@PatchMapping
	public ResponseEntity<String> updateCustomer(@RequestParam long customerId
		,@RequestBody CustomerUpdateDto dto) {
		customerService.updateCustomer(customerId, dto);
		return ResponseEntity.ok("회원정보가 수정되었습니다.");
	}

	@DeleteMapping
	public ResponseEntity<String> deleteCustomer(@RequestParam long customerId) {
		customerService.deleteCustomer(customerId);
		return ResponseEntity.ok("회원 탈퇴되었습니다.");
	}
}
