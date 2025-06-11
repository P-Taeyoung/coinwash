package pp.coinwash.machine.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.domain.repository.MachineRepository;
import pp.coinwash.machine.domain.type.UsageStatus;
import pp.coinwash.point.application.PointHistoryApplication;
import pp.coinwash.point.domain.dto.PointHistoryRequestDto;

@Service
@RequiredArgsConstructor
public class ReservingMachineService {

	private final MachineRepository machineRepository;

	private final PointHistoryApplication pointHistoryApplication;

	@Transactional
	public Machine reserveMachine(long machineId, long customerId) {

		//예약금
		pointHistoryApplication.usePoints(
			PointHistoryRequestDto.usePoint(customerId, 100));

		Machine machine = getCanReserveMachine(machineId);

		machine.reserve(customerId);

		return machine;
	}

	@Transactional
	public Machine cancelReserveMachine(long machineId, long customerId) {

		Machine machine = getCancelReserveMachine(machineId, customerId);

		machine.reset();

		return machine;
	}

	private Machine getCanReserveMachine(long machineId) {

		return machineRepository.findUsableMachineByMachineId(machineId)
			.orElseThrow(() -> new RuntimeException("예약가능한 기계 정보가 없습니다."));
	}

	private Machine getCancelReserveMachine(long machineId, long customerId) {

		Machine machine = machineRepository.findUsableMachineByMachineId(machineId)
			.orElseThrow(() -> new RuntimeException("기계 정보가 없습니다."));

		if (machine.getCustomerId() != customerId) {
			throw new RuntimeException("예약 취소 권한이 없습니다.");
		}

		return machine;
	}
}
