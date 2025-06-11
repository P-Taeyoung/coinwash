package pp.coinwash.machine.controller;

import java.util.List;

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
import pp.coinwash.machine.application.MachineManageApplication;
import pp.coinwash.machine.domain.dto.MachineRegisterDto;
import pp.coinwash.machine.domain.dto.MachineResponseDto;
import pp.coinwash.machine.domain.dto.MachineUpdateDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/machines")
public class MachineManageController {

	private final MachineManageApplication machineManageApplication;

	@PostMapping
	public ResponseEntity<String> registerMachine(
		@RequestBody List<MachineRegisterDto> dtos,
		@RequestParam long laundryId,
		@RequestParam long ownerId) {
		machineManageApplication.registerMachines(dtos, laundryId, ownerId);

		return ResponseEntity.ok("기계정보가 정상적으로 저장되었습니다.");
	}

	@GetMapping
	public ResponseEntity<List<MachineResponseDto>> getMachines(
		@RequestParam long laundryId) {
		return ResponseEntity.ok(machineManageApplication.getMachinesByLaundryId(laundryId));
	}

	@PatchMapping
	public ResponseEntity<String> updateMachine(
		@RequestBody MachineUpdateDto dto,
		@RequestParam long ownerId) {
		machineManageApplication.updateMachine(dto, ownerId);
		return ResponseEntity.ok("기계정보가 정상적으로 수정되었습니다.");
	}

	@DeleteMapping
	public ResponseEntity<String> deleteMachine(
		@RequestParam long machineId,
		@RequestParam long ownerId) {
		machineManageApplication.deleteMachine(machineId, ownerId);
		return ResponseEntity.ok("기계정보가 정상적으로 삭제되었습니다.");
	}


}
