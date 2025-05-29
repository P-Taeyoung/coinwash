package pp.coinwash.machine.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pp.coinwash.machine.domain.dto.UsingDryingDto;
import pp.coinwash.machine.domain.dto.UsingWashingDto;
import pp.coinwash.machine.service.ReservingMachineService;
import pp.coinwash.machine.service.UsingMachineService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservation")
public class ReservingMachineController {

	private final ReservingMachineService reservingMachineService;

	@PutMapping
	public ResponseEntity<String> reserveMachine(@RequestParam long machineId,
		@RequestParam long customerId) {

		reservingMachineService.reserveMachine(machineId, customerId);

		return ResponseEntity.ok("예약되었습니다.");
	}

	@PatchMapping
	public ResponseEntity<String> cancelReserveMachine(@RequestParam long machineId,
		@RequestParam long customerId) {

		reservingMachineService.cancelReserveMachine(machineId, customerId);

		return ResponseEntity.ok("예약이 취소되었습니다.");
	}
}
