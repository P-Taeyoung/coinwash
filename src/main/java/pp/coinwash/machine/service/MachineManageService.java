package pp.coinwash.machine.service;

import static pp.coinwash.common.exception.ErrorCode.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pp.coinwash.common.exception.CustomException;
import pp.coinwash.common.exception.ErrorCode;
import pp.coinwash.laundry.domain.entity.Laundry;
import pp.coinwash.laundry.domain.repository.LaundryRepository;
import pp.coinwash.machine.domain.dto.MachineRegisterDto;
import pp.coinwash.machine.domain.dto.MachineResponseDto;
import pp.coinwash.machine.domain.dto.MachineUpdateDto;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.domain.repository.MachineRepository;
import pp.coinwash.machine.domain.type.UsageStatus;

@Service
@RequiredArgsConstructor
public class MachineManageService {

	private final MachineRepository machineRepository;
	private final LaundryRepository laundryRepository;

	public List<Machine> registerMachines(List<MachineRegisterDto> dtos, long laundryId,long ownerId) {
		List<Machine> machines = new ArrayList<>();

		Laundry laundry = getValidateLaundry(laundryId, ownerId);

		for (MachineRegisterDto dto : dtos) {
			machines.add(machineRepository.save(Machine.of(dto,laundry)));
		}

		return machines;
	}

	public List<MachineResponseDto> getMachinesByLaundryId(long laundryId) {

		List<Machine> machines = machineRepository.findByLaundryLaundryIdAndDeletedAtIsNull(laundryId);

		for (Machine machine : machines) {
			if (machine.getEndTime() != null
			&& machine.getEndTime().isBefore(LocalDateTime.now())
			&& machine.getUsageStatus() != UsageStatus.UNUSABLE) {

				machine.reset();
			}
		}

		return machines.stream().map(MachineResponseDto::from).collect(Collectors.toList());
	}

	@Transactional
	public Machine updateMachine(MachineUpdateDto updateDto, long ownerId) {
		Machine machine = getValidateMachine(updateDto.machineId(), ownerId);

		machine.updateOf(updateDto);

		return machine;
	}

	@Transactional
	public void deleteMachine(long machineId, long ownerId) {
		getValidateMachine(machineId, ownerId).delete();
	}


	private Laundry getValidateLaundry(long laundryId, long ownerId) {
		return laundryRepository.findByLaundryIdAndOwnerIdAndDeletedAtIsNull(laundryId, ownerId)
			.orElseThrow(() -> new CustomException(NO_AUTHORIZED_MACHINE_FOUND));
	}

	private Machine getValidateMachine(long machineId, long ownerId) {

		return machineRepository.findByMachineIdAndLaundryOwnerId(machineId, ownerId)
			.orElseThrow(() -> new CustomException(NO_AUTHORIZED_MACHINE_FOUND));
	}
}
