package pp.coinwash.machine.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pp.coinwash.machine.application.MachineApplication;
import pp.coinwash.machine.domain.dto.UsingDryingDto;
import pp.coinwash.machine.domain.dto.UsingWashingDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/machines")
public class MachineController {

	private final MachineApplication machineApplication;

	@PostMapping("/washing")
	public ResponseEntity<String> useWashingMachine(@RequestParam long customerId,
		@RequestBody UsingWashingDto dto) {

		machineApplication.useWashing(customerId, dto);

		return ResponseEntity.ok("세탁을 시작합니다.");
	}

	@PostMapping("/drying")
	public ResponseEntity<String> useDryingMachine(@RequestParam long customerId,
		@RequestBody UsingDryingDto dto) {

		machineApplication.useDrying(customerId, dto);

		return ResponseEntity.ok("건조를 시작합니다.");
	}

	@PutMapping("/reservations")
	public ResponseEntity<String> reserveMachine(@RequestParam long machineId,
		@RequestParam long customerId) {

		machineApplication.reserveMachine(machineId, customerId);

		return ResponseEntity.ok("예약되었습니다.");
	}

	@PatchMapping("/reservations")
	public ResponseEntity<String> cancelReserveMachine(@RequestParam long machineId,
		@RequestParam long customerId) {

		machineApplication.cancelReservingMachine(machineId, customerId);

		return ResponseEntity.ok("예약이 취소되었습니다.");
	}
}
