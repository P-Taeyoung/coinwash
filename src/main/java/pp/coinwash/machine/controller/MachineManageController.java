package pp.coinwash.machine.controller;

import java.util.List;

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
import pp.coinwash.machine.application.MachineManageApplication;
import pp.coinwash.machine.domain.dto.MachineRegisterDto;
import pp.coinwash.machine.domain.dto.MachineResponseDto;
import pp.coinwash.machine.domain.dto.MachineUpdateDto;
import pp.coinwash.security.dto.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner/machines")
@Tag(name = "기계 관리", description = "기계 관리 API")
public class MachineManageController {

	private final MachineManageApplication machineManageApplication;

	@Operation(
		summary = "기계 정보 등록",
		tags = {"기계 관리"},
		description = "점주가 세탁소를 등록 후 기계 정보를 등록. 기계는 여러 개를 한 번에 등록할 수 있도록 함."
	)
	@PostMapping
	public ResponseEntity<String> registerMachine(
		@RequestBody List<MachineRegisterDto> dtos,
		@RequestParam long laundryId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		machineManageApplication.registerMachines(dtos, laundryId, userDetails.getUserId());

		return ResponseEntity.ok("기계정보가 정상적으로 저장되었습니다.");
	}



	@Operation(
		summary = "기계 정보 수정",
		tags = {"기계 관리"},
		description = "점주가 자신의 세탁소 기계 정보를 수정. 해당 세탁소의 점주 권한을 지닌 사용자만 수정 가능"
	)
	@PatchMapping
	public ResponseEntity<String> updateMachine(
		@RequestBody MachineUpdateDto dto,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		machineManageApplication.updateMachine(dto, userDetails.getUserId());
		return ResponseEntity.ok("기계정보가 정상적으로 수정되었습니다.");
	}

	@Operation(
		summary = "기계 정보 삭제",
		tags = {"기계 관리"},
		description = "점주가 자신의 세탁소 기계 정보를 삭제. 세탁소와 마찬가지로 DB 에서 완전히 삭제되는 것이 아닌 삭제일자 추가."
	)
	@DeleteMapping
	public ResponseEntity<String> deleteMachine(
		@RequestParam long machineId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		machineManageApplication.deleteMachine(machineId, userDetails.getUserId());
		return ResponseEntity.ok("기계정보가 정상적으로 삭제되었습니다.");
	}


}
