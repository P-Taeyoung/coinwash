package pp.coinwash.history.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pp.coinwash.common.dto.PagedResponseDto;
import pp.coinwash.machine.domain.repository.MachineRepository;
import pp.coinwash.history.domain.dto.UsageHistoryRequestDto;
import pp.coinwash.history.domain.dto.UsageHistoryResponseDto;
import pp.coinwash.history.service.UsageHistoryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/usage-history")
public class UsageHistoryController {

	private final UsageHistoryService usageHistoryService;
	private final MachineRepository machineRepository;

	@GetMapping
	public ResponseEntity<PagedResponseDto<UsageHistoryResponseDto>> getUsageHistories(
		@RequestParam long customerId,
		@PageableDefault(sort = "historyId", direction = Sort.Direction.DESC)
		Pageable pageable) {

		return ResponseEntity.ok(usageHistoryService
			.getUsageHistoriesByCustomerId(customerId, pageable));
	}


	//TEST 용
	@PostMapping
	public ResponseEntity<String> createUsageHistory(
		@RequestBody UsageHistoryRequestDto usageHistoryRequestDto
	) {
		long machineId = 1;

		usageHistoryService.createUsageHistory(usageHistoryRequestDto
			, machineRepository.findById(machineId)
				.orElseThrow(() -> new RuntimeException("Invalid machine id: " + machineId)));

		return ResponseEntity.ok("사용 내역이 생성되었습니다.");
	}
}
