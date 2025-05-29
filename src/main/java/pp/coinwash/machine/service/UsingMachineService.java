package pp.coinwash.machine.service;

import static pp.coinwash.machine.domain.type.MachineType.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pp.coinwash.machine.domain.dto.UsingDryingDto;
import pp.coinwash.machine.domain.dto.UsingWashingDto;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.domain.repository.MachineRepository;
import pp.coinwash.machine.domain.type.MachineType;
import pp.coinwash.machine.domain.type.UsageStatus;

@Service
@RequiredArgsConstructor
public class UsingMachineService {

	private final MachineRepository machineRepository;

	@Transactional
	public void useWashing(long customerId, UsingWashingDto usingWashingDto) {

		Machine machine = verifyUsableMachine(
			usingWashingDto.machineId(), customerId, WASHING);

		machine.useWashing(customerId, usingWashingDto.course());
	}

	@Transactional
	public void useDrying(long customerId, UsingDryingDto usingDryingDto) {

		Machine machine = verifyUsableMachine(
			usingDryingDto.machineId(), customerId, DRYING);

		machine.useDrying(customerId, usingDryingDto.course());
	}

	//상태초기화
	@Transactional
	public void resetStatus(long machineId) {
		machineRepository.findById(machineId)
			.orElseThrow(() -> new RuntimeException("해당하는 기계 정보가 없습니다."))
			.reset();
	}



	private Machine verifyUsableMachine(long machineId, long customerId, MachineType machineType) {

		Machine machine = machineRepository.findMachineByMachineId(machineId, machineType)
			.orElseThrow(() -> new RuntimeException("해당하는 기계 정보가 없습니다."));

		if (machine.getUsageStatus() == UsageStatus.USING
			|| machine.getUsageStatus() == UsageStatus.UNUSABLE) {
			throw new RuntimeException("현재 사용할 수 없는 기계입니다.");
		}

		// 예약 중이라면 예약자 확인
		if (machine.getUsageStatus() == UsageStatus.RESERVING
		&& machine.getCustomerId() != customerId) {
			throw new RuntimeException("예약자가 아닙니다.");
		}

		return machine;
	}
}
