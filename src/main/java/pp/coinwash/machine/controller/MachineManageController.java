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
import pp.coinwash.machine.domain.dto.MachineRegisterDto;
import pp.coinwash.machine.domain.dto.MachineResponseDto;
import pp.coinwash.machine.domain.dto.MachineUpdateDto;
import pp.coinwash.machine.service.MachineManageService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/machines")
public class MachineManageController {

	private final MachineManageService machineManageService;

	@PostMapping
	public ResponseEntity<String> registerMachine(
		@RequestBody List<MachineRegisterDto> dtos,
		@RequestParam long laundryId,
		@RequestParam long ownerId) {
		machineManageService.registerMachines(dtos, laundryId, ownerId);

		return ResponseEntity.ok("기계정보가 정상적으로 저장되었습니다.");
	}

	@GetMapping
	public ResponseEntity<List<MachineResponseDto>> getMachines(
		@RequestParam long laundryId,
		@RequestParam long ownerId) {
		return ResponseEntity.ok(machineManageService.getMachinesByLaundryId(laundryId, ownerId));
	}

	@PatchMapping
	public ResponseEntity<String> updateMachine(
		@RequestBody MachineUpdateDto dto,
		@RequestParam long ownerId) {
		machineManageService.updateMachine(dto, ownerId);
		return ResponseEntity.ok("기계정보가 정상적으로 수정되었습니다.");
	}

	@DeleteMapping
	public ResponseEntity<String> deleteMachine(
		@RequestParam long laundryId,
		@RequestParam long ownerId) {
		machineManageService.deleteMachine(laundryId, ownerId);
		return ResponseEntity.ok("기계정보가 정상적으로 삭제되었습니다.");
	}


}
