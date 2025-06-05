package pp.coinwash.machine.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pp.coinwash.laundry.domain.entity.Laundry;
import pp.coinwash.laundry.domain.repository.LaundryRepository;
import pp.coinwash.machine.domain.dto.MachineRegisterDto;
import pp.coinwash.machine.domain.dto.MachineResponseDto;
import pp.coinwash.machine.domain.dto.MachineUpdateDto;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.domain.repository.MachineRepository;

@Service
@RequiredArgsConstructor
public class MachineManageService {

	private final MachineRepository machineRepository;
	private final LaundryRepository laundryRepository;

	public void registerMachines(List<MachineRegisterDto> dtos, long laundryId,long ownerId) {

		Laundry laundry = getValidateLaundry(laundryId, ownerId);

		for (MachineRegisterDto dto : dtos) {
			machineRepository.save(Machine.of(dto,laundry));
		}
	}

	public List<MachineResponseDto> getMachinesByLaundryId(long laundryId) {

		List<Machine> machines = machineRepository.findByLaundryLaundryIdAndDeletedAtIsNull(laundryId);

		return machines.stream().map(MachineResponseDto::from).collect(Collectors.toList());
	}

	@Transactional
	public void updateMachine(MachineUpdateDto updateDto, long ownerId) {
		getValidateMachine(updateDto.machineId(), ownerId).updateOf(updateDto);
	}

	@Transactional
	public void deleteMachine(long machineId, long ownerId) {
		getValidateMachine(machineId, ownerId).delete();
	}


	private Laundry getValidateLaundry(long laundryId, long ownerId) {
		return laundryRepository.findByLaundryIdAndOwnerIdAndDeletedAtIsNull(laundryId, ownerId)
			.orElseThrow(() -> new RuntimeException("해당하는 세탁소 정보를 찾을 수 없습니다."));
	}

	private void verifyValidateLaundry(long laundryId, long ownerId) {
		if (!laundryRepository.existsByLaundryIdAndOwnerIdAndDeletedAtIsNull(laundryId, ownerId)) {
			throw new RuntimeException("세탁소Id, 점장Id 와일치하는 세탁소 정보를 찾을 수 없습니다.");
		}
	}

	private Machine getValidateMachine(long machineId, long ownerId) {

		return machineRepository.findByMachineIdAndLaundryOwnerId(machineId, ownerId)
			.orElseThrow(() -> new RuntimeException("권한을 지닌 기계 정보를 찾을 수 없습니다."));
	}
}
