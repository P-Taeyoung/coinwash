package pp.coinwash.history.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pp.coinwash.common.dto.PagedResponseDto;
import pp.coinwash.machine.domain.repository.MachineRepository;
import pp.coinwash.history.domain.dto.HistoryRequestDto;
import pp.coinwash.history.domain.dto.HistoryResponseDto;
import pp.coinwash.history.service.HistoryService;
import pp.coinwash.security.dto.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/history")
@Tag(name = "기계 사용/예약 내역 ", description = "기계 사용/예약 내역 조회 API")
public class HistoryController {

	private final HistoryService historyService;
	private final MachineRepository machineRepository;

	@Operation(
		summary = "기계 사용/예약 내역 조회",
		tags = {"기계 사용/예약 내역"},
		description = "사용자의 기계 사용/예약 내역 조회, Page 형태로 반환하여 페이지네이션할 수 있도록 함."
	)
	@GetMapping
	public ResponseEntity<PagedResponseDto<HistoryResponseDto>> getUsageHistories(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PageableDefault(sort = "historyId", direction = Sort.Direction.DESC)
		Pageable pageable) {

		return ResponseEntity.ok(historyService
			.getUsageHistoriesByCustomerId(userDetails.getUserId(), pageable));
	}


	//TEST 용
	@PostMapping
	public ResponseEntity<String> createUsageHistory(
		@RequestBody HistoryRequestDto historyRequestDto
	) {
		long machineId = 1;

		historyService.createUsageHistory(historyRequestDto
			, machineRepository.findById(machineId)
				.orElseThrow(() -> new RuntimeException("Invalid machine id: " + machineId)));

		return ResponseEntity.ok("사용 내역이 생성되었습니다.");
	}
}
