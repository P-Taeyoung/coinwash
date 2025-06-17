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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pp.coinwash.common.dto.PagedResponseDto;
import pp.coinwash.laundry.domain.dto.LaundryRegisterDto;
import pp.coinwash.laundry.domain.dto.LaundryResponseDto;
import pp.coinwash.laundry.domain.dto.LaundryUpdateDto;
import pp.coinwash.laundry.service.LaundryManageService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner/laundries")
@Tag(name = "세탁소 관리", description = "세탁소 관리 API")
public class LaundryManageController {

	private final LaundryManageService laundryManageService;

	@Operation(
		summary = "세탁소 등록",
		tags = {"세탁소 관리"},
		description = "점주 권한을 지닌 사용자가 세탁소를 등록, 이 때 500M 내 같은 세탁소가 존재한다면 등록 불가."
	)
	@PostMapping
	public ResponseEntity<String> registerLaundry(@RequestParam long ownerId
	, @RequestBody LaundryRegisterDto dto) {

		laundryManageService.registerLaundry(dto, ownerId);
		return ResponseEntity.ok("매장 등록이 완료되었습니다.");
	}

	@Operation(
		summary = "점주 세탁소 조회",
		tags = {"세탁소 관리"},
		description = "점주 세탁소 정보를 조회, 점주가 담당하고 있는 세탁소 목록을 반환."
	)
	@GetMapping
	public ResponseEntity<PagedResponseDto<LaundryResponseDto>> getLaundriesByOwnerId(
		@RequestParam long ownerId,
		@PageableDefault(sort = "laundryId", direction = Sort.Direction.DESC)
		Pageable pageable) {

		return ResponseEntity.ok(laundryManageService.getLaundriesByOwnerId(ownerId, pageable));
	}

	@Operation(
		summary = "세탁소 정보 수정",
		tags = {"세탁소 관리"},
		description = "점주가 본인의 세탁소 정보를 수정."
	)
	@PatchMapping
	public ResponseEntity<String> updateLaundry(
		@RequestParam long laundryId,
		@RequestParam long ownerId,
		@RequestBody LaundryUpdateDto dto) {

		laundryManageService.updateLaundry(laundryId, ownerId, dto);
		return ResponseEntity.ok("매장 정보 수정이 완료되었습니다.");
	}

	@Operation(
		summary = "세탁소 정보 삭제",
		tags = {"세탁소 관리"},
		description = "점주가 본인의 세탁소 정보를 삭제. 이 때 DB 에서 완전히 삭제되는 것이 아니라 삭제일자 값이 추가됨."
	)
	@DeleteMapping
	public ResponseEntity<String> deleteLaundry(
		@RequestParam long laundryId,
		@RequestParam long ownerId) {

		laundryManageService.deleteLaundry(laundryId, ownerId);
		return ResponseEntity.ok("매장 정보가 삭제되었습니다.");
	}

	@Operation(
		summary = "세탁소 운영 현황 변경",
		tags = {"세탁소 관리"},
		description = "점주가 본인의 세탁소 운영 현황을 변경. 사정이 있을 경우 세탁소 운영 현황을 변경할 수 있음(정상 영업, 영업 종료)."
	)
	@PatchMapping("/status")
	public ResponseEntity<String> changeLaundryStatus(
		@RequestParam long laundryId,
		@RequestParam long ownerId) {

		laundryManageService.changeLaundryStatus(laundryId, ownerId);
		return ResponseEntity.ok("매장 운영 현황이 변경되었습니다.");
	}
}
