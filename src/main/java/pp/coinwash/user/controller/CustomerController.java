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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pp.coinwash.user.domain.dto.CustomerResponseDto;
import pp.coinwash.user.domain.dto.UserSignInDto;
import pp.coinwash.user.domain.dto.CustomerSignUpDto;
import pp.coinwash.user.domain.dto.CustomerUpdateDto;
import pp.coinwash.user.service.CustomerService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customer")
@Tag(name = "고객 회원 관리", description = "고객 회원 관리 API")
public class CustomerController {

	private final CustomerService customerService;

	@Operation(
		summary = "고객 회원 로그인",
		tags = {"고객 회원 관리"}
	)
	@PostMapping("/signin")
	public ResponseEntity<String> signIn(@RequestBody UserSignInDto dto) {

		return ResponseEntity.ok(customerService.signIn(dto));
	}

	@Operation(
		summary = "고객 회원가입",
		tags = {"고객 회원 관리"}
	)
	@PostMapping("/signup")
	public ResponseEntity<String> signUpCustomer(@RequestBody CustomerSignUpDto dto) {
		customerService.signUp(dto);
		return ResponseEntity.ok("회원가입이 완료되었습니다.");
	}

	@Operation(
		summary = "고객 회원 정보 조회",
		tags = {"고객 회원 관리"}
	)
	@GetMapping
	//TODO 추후 ContextHolder 에서 고객id 를 가져올 수 있도록
	public ResponseEntity<CustomerResponseDto> getCustomers(@RequestParam long customerId) {
		return ResponseEntity.ok(customerService.getCustomer(customerId));
	}

	@Operation(
		summary = "고객 회원 정보 수정",
		tags = {"고객 회원 관리"},
		description = "고객 회원 전화번호, 주소 정보 수정 가능."
	)
	@PatchMapping
	public ResponseEntity<String> updateCustomer(@RequestParam long customerId
		,@RequestBody CustomerUpdateDto dto) {
		customerService.updateCustomer(customerId, dto);
		return ResponseEntity.ok("회원정보가 수정되었습니다.");
	}

	@Operation(
		summary = "고객 회원 탈퇴",
		tags = {"고객 회원 관리"},
		description = "고객 회원 탈퇴 시 DB 에서 정보가 완전히 삭제되는 것이 아닌 삭제일자 추가."
	)
	@DeleteMapping
	public ResponseEntity<String> deleteCustomer(@RequestParam long customerId) {
		customerService.deleteCustomer(customerId);
		return ResponseEntity.ok("회원 탈퇴되었습니다.");
	}
}
