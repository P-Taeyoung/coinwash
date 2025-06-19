package pp.coinwash.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import pp.coinwash.security.dto.CustomUserDetails;
import pp.coinwash.user.domain.dto.OwnerResponseDto;
import pp.coinwash.user.domain.dto.OwnerSignUpDto;
import pp.coinwash.user.domain.dto.OwnerUpdateDto;
import pp.coinwash.user.domain.dto.UserSignInDto;
import pp.coinwash.user.service.OwnerService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner")
@Tag(name = "점주 회원 관리", description = "점주 회원 관리 API")
public class OwnerController {

	private final OwnerService ownerService;

	@Operation(
		summary = "점주 회원 로그인",
		tags = {"점주 회원 관리"}
	)
	@PostMapping("/signin")
	public ResponseEntity<String> signIn(@RequestBody UserSignInDto dto) {
		return ResponseEntity.ok(ownerService.ownerSignIn(dto));
	}

	@Operation(
		summary = "점주 회원가입",
		tags = {"점주 회원 관리"}
	)
	@PostMapping("/signup")
	public ResponseEntity<String> signUp(@RequestBody OwnerSignUpDto dto) {
		ownerService.ownerSignUp(dto);
		return ResponseEntity.ok("회원가입이 완료되었습니다.");
	}

	@Operation(
		summary = "점주 회원 정보 조회",
		tags = {"점주 회원 관리"}
	)
	@GetMapping
	public ResponseEntity<OwnerResponseDto> getOwner(@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ResponseEntity.ok(ownerService.getOwner(userDetails.getUserId()));
	}

	@Operation(
		summary = "점주 회원 정보 수정",
		tags = {"점주 회원 관리"},
		description = "점주 회원 전화번호 수정 가능."
	)
	@PatchMapping
	public ResponseEntity<String> updateOwner(@AuthenticationPrincipal CustomUserDetails userDetails
		,@RequestBody OwnerUpdateDto dto) {
		ownerService.updateOwner(userDetails.getUserId(), dto);
		return ResponseEntity.ok("회원정보가 수정되었습니다.");
	}

	@Operation(
		summary = "점주 회원 탈퇴",
		tags = {"점주 회원 관리"},
		description = "점주 회원 탈퇴 시 DB 에서 정보가 완전히 삭제되는 것이 아닌 삭제일자 추가."
	)
	@DeleteMapping
	public ResponseEntity<String> deleteOwner(@AuthenticationPrincipal CustomUserDetails userDetails) {
		ownerService.deleteOwner(userDetails.getUserId());
		return ResponseEntity.ok("회원 탈퇴되었습니다.");
	}
}
