package pp.coinwash.machine.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pp.coinwash.machine.domain.dto.UsingDryingDto;
import pp.coinwash.machine.domain.dto.UsingWashingDto;
import pp.coinwash.machine.service.UsingMachineService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/usage")
public class UsingMachineController {

	private final UsingMachineService usingMachineService;

	@PostMapping("/washing")
	public ResponseEntity<String> useWashingMachine(@RequestParam long customerId,
		@RequestBody UsingWashingDto dto) {

		usingMachineService.useWashing(customerId, dto);

		return ResponseEntity.ok("세탁을 시작합니다.");
	}

	@PostMapping("/drying")
	public ResponseEntity<String> useDryingMachine(@RequestParam long customerId,
		@RequestBody UsingDryingDto dto) {

		usingMachineService.useDrying(customerId, dto);

		return ResponseEntity.ok("건조를 시작합니다.");
	}

	//TEST 용
	@PatchMapping
	public ResponseEntity<String> resetMachine(@RequestParam long machineId) {

		usingMachineService.resetStatus(machineId);

		return ResponseEntity.ok("기계 초기화가 완료되었습니다.");
	}
}
