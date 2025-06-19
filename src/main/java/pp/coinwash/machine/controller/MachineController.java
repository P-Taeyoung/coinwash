package pp.coinwash.machine.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pp.coinwash.machine.application.MachineApplication;
import pp.coinwash.machine.domain.dto.UsingDryingDto;
import pp.coinwash.machine.domain.dto.UsingWashingDto;
import pp.coinwash.security.dto.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/machines")
@Tag(name = "기계 사용/예약", description = "기계 사용/예약 API")
public class MachineController {

	private final MachineApplication machineApplication;

	@Operation(
		summary = "세탁기 사용",
		tags = {"기계 사용/예약"},
		description = "세탁기 사용, 원하는 코스에 따라 필요한 포인트가 다름. 포인트 부족 시 사용 불가."
	)
	@PostMapping("/washing")
	public ResponseEntity<String> useWashingMachine(@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody UsingWashingDto dto) {

		machineApplication.useWashing(userDetails.getUserId(), dto);

		return ResponseEntity.ok("세탁을 시작합니다.");
	}

	@Operation(
		summary = "건조기 사용",
		tags = {"기계 사용/예약"},
		description = "건조기 사용, 원하는 코스에 따라 필요한 포인트가 다름. 포인트 부족 시 사용 불가."
	)
	@PostMapping("/drying")
	public ResponseEntity<String> useDryingMachine(@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody UsingDryingDto dto) {

		machineApplication.useDrying(userDetails.getUserId(), dto);

		return ResponseEntity.ok("건조를 시작합니다.");
	}

	@Operation(
		summary = "기계 예약",
		tags = {"기계 사용/예약"},
		description = "기계 예약, 예약 시 15분 내 사용해야 함. 또한 예약 시 예약 포인트 필요."
			+ " 15분 내 사용하는 경우 예약 포인트 제외한 나머지 포인트 지급 후 사용 가능. "
	)
	@PutMapping("/reservations")
	public ResponseEntity<String> reserveMachine(@RequestParam long machineId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		machineApplication.reserveMachine(machineId, userDetails.getUserId());

		return ResponseEntity.ok("예약되었습니다.");
	}

	@Operation(
		summary = "기계 예약 취소",
		tags = {"기계 사용/예약"},
		description = "기계 예약 취소, 예약된 기계를 예약 취소 가능. 예약 취소 시 지불한 예약 포인트 절반을 환급."
	)
	@PatchMapping("/reservations")
	public ResponseEntity<String> cancelReserveMachine(@RequestParam long machineId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		machineApplication.cancelReservingMachine(machineId, userDetails.getUserId());

		return ResponseEntity.ok("예약이 취소되었습니다.");
	}
}
