package pp.coinwash.laundry.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import pp.coinwash.common.dto.PagedResponseDto;
import pp.coinwash.laundry.domain.dto.LaundryRegisterDto;
import pp.coinwash.laundry.domain.dto.LaundryResponseDto;
import pp.coinwash.laundry.domain.dto.LaundryUpdateDto;
import pp.coinwash.laundry.service.LaundryManageService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner/laundries")
public class LaundryManageController {

	private final LaundryManageService laundryManageService;

	@PostMapping
	public ResponseEntity<String> registerLaundry(@RequestParam long ownerId
	, @RequestBody LaundryRegisterDto dto) {

		laundryManageService.registerLaundry(dto, ownerId);
		return ResponseEntity.ok("매장 등록이 완료되었습니다.");
	}

	@GetMapping
	public ResponseEntity<PagedResponseDto<LaundryResponseDto>> getLaundriesByOwnerId(
		@RequestParam long ownerId,
		@PageableDefault(sort = "laundryId", direction = Sort.Direction.DESC)
		Pageable pageable) {

		return ResponseEntity.ok(laundryManageService.getLaundriesByOwnerId(ownerId, pageable));
	}

	@PatchMapping
	public ResponseEntity<String> updateLaundry(
		@RequestParam long laundryId,
		@RequestParam long ownerId,
		@RequestBody LaundryUpdateDto dto) {

		laundryManageService.updateLaundry(laundryId, ownerId, dto);
		return ResponseEntity.ok("매장 정보 수정이 완료되었습니다.");
	}

	@DeleteMapping
	public ResponseEntity<String> deleteLaundry(
		@RequestParam long laundryId,
		@RequestParam long ownerId) {

		laundryManageService.deleteLaundry(laundryId, ownerId);
		return ResponseEntity.ok("매장 정보가 삭제되었습니다.");
	}

	@PatchMapping("/status")
	public ResponseEntity<String> changeLaundryStatus(
		@RequestParam long laundryId,
		@RequestParam long ownerId) {

		laundryManageService.changeLaundryStatus(laundryId, ownerId);
		return ResponseEntity.ok("매장 운영 현황이 변경되었습니다.");
	}
}
