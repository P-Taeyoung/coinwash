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
import pp.coinwash.user.domain.dto.OwnerResponseDto;
import pp.coinwash.user.domain.dto.OwnerSignUpDto;
import pp.coinwash.user.domain.dto.OwnerUpdateDto;
import pp.coinwash.user.service.OwnerService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/owner")
public class OwnerController {

	private final OwnerService ownerService;

	@PostMapping("/signup")
	public ResponseEntity<String> saveCustomer(@RequestBody OwnerSignUpDto dto) {
		ownerService.ownerSignUp(dto);
		return ResponseEntity.ok("회원가입이 완료되었습니다.");
	}

	@GetMapping
	//TODO 추후 ContextHolder 에서 고객id 를 가져올 수 있도록
	public ResponseEntity<OwnerResponseDto> getCustomers(@RequestParam long ownerId) {
		return ResponseEntity.ok(ownerService.getOwner(ownerId));
	}

	@PatchMapping
	public ResponseEntity<String> updateCustomer(@RequestParam long ownerId
		,@RequestBody OwnerUpdateDto dto) {
		ownerService.updateOwner(ownerId, dto);
		return ResponseEntity.ok("회원정보가 수정되었습니다.");
	}

	@DeleteMapping
	public ResponseEntity<String> deleteCustomer(@RequestParam long ownerId) {
		ownerService.deleteOwner(ownerId);
		return ResponseEntity.ok("회원 탈퇴되었습니다.");
	}
}
