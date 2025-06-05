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
import pp.coinwash.point.application.PointHistoryApplication;
import pp.coinwash.point.domain.dto.PointHistoryRequestDto;

@Service
@RequiredArgsConstructor
public class UsingMachineService {

	private final MachineRepository machineRepository;

	private final PointHistoryApplication pointHistoryApplication;

	@Transactional
	public void useWashing(long customerId, UsingWashingDto usingWashingDto) {

		Machine machine = verifyUsableMachine(
			usingWashingDto.machineId(), customerId, WASHING);

		pointHistoryApplication.usePoints(
			PointHistoryRequestDto.usePoint(customerId,
				usingWashingDto.course().getFee()));

		machine.useWashing(customerId, usingWashingDto.course());
	}

	@Transactional
	public void useDrying(long customerId, UsingDryingDto usingDryingDto) {

		Machine machine = verifyUsableMachine(
			usingDryingDto.machineId(), customerId, DRYING);

		pointHistoryApplication.usePoints(
			PointHistoryRequestDto.usePoint(customerId,
				usingDryingDto.course().getFee()));

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

		Machine machine = machineRepository.findUsableMachineWithLock(machineId, machineType)
			.orElseThrow(() -> new RuntimeException("해당하는 기계 정보가 없습니다."));

		if (machine.getUsageStatus() == UsageStatus.USING
			|| machine.getUsageStatus() == UsageStatus.UNUSABLE) {
			throw new RuntimeException("현재 사용할 수 없는 기계입니다.");
		}

		// 예약 중이라면 예약자 확인
		if (machine.getUsageStatus() == UsageStatus.RESERVING) {

			if (machine.getCustomerId() != customerId) {
				throw new RuntimeException("예약자가 아닙니다.");
			}
			//예약금 환급
			pointHistoryApplication.earnPoints(
				PointHistoryRequestDto.earnPoint(customerId,
					100));
		}

		return machine;
	}
}
