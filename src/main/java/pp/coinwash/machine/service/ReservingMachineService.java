package pp.coinwash.machine.service;

import static pp.coinwash.machine.event.MachineEvent.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pp.coinwash.history.domain.dto.HistoryRequestDto;
import pp.coinwash.history.event.HistoryEvent;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.domain.repository.MachineRepository;
import pp.coinwash.machine.event.MachineEvent;
import pp.coinwash.point.application.PointHistoryApplication;
import pp.coinwash.point.domain.dto.PointHistoryRequestDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservingMachineService {

	private final MachineRepository machineRepository;

	private final PointHistoryApplication pointHistoryApplication;

	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public Machine reserveMachine(long machineId, long customerId) {

		//예약금
		pointHistoryApplication.usePoints(
			PointHistoryRequestDto.usePoint(customerId, 100));

		Machine machine = getCanReserveMachine(machineId);

		machine.reserve(customerId);

		publishHistoryEventSafely(
			HistoryRequestDto.createReservationHistory(
				customerId,
				LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)),
			machine);

		publishSchedulerEventSafely(machine, ScheduleType.RESERVING);

		return machine;
	}

	@Transactional
	public Machine cancelReserveMachine(long machineId, long customerId) {

		Machine machine = getCancelReserveMachine(machineId, customerId);

		machine.reset();

		publishHistoryEventSafely(
			HistoryRequestDto.createCancelReservationHistory(customerId),
			machine);

		publishSchedulerEventSafely(machine, ScheduleType.CANCEL_RESERVING);

		return machine;
	}

	private Machine getCanReserveMachine(long machineId) {

		return machineRepository.findUsableMachineByMachineId(machineId, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
			.orElseThrow(() -> new RuntimeException("예약가능한 기계 정보가 없습니다."));
	}

	private Machine getCancelReserveMachine(long machineId, long customerId) {

		return machineRepository.findReserveMachine(machineId, customerId)
			.orElseThrow(() -> new RuntimeException("예약한 기계 정보가 없습니다."));
	}

	private void publishHistoryEventSafely(HistoryRequestDto dto, Machine machine) {
		try {
			eventPublisher.publishEvent(HistoryEvent.of(dto, machine));
		} catch (Exception e) {
			//이벤트 발행 실패해도 메인 로직에 영향 없도록
			log.warn("이벤트 발행 실패하지만 메인 기능은 정상 처리됨: {}", e.getMessage());
		}
	}

	private void publishSchedulerEventSafely(Machine machine, ScheduleType scheduleType) {
		try {

			if (scheduleType == ScheduleType.RESERVING) {
				eventPublisher.publishEvent(reservingMachineEvent(machine));
			} else {
				eventPublisher.publishEvent(cancelReservingMachineEvent(machine));
			}

		} catch (Exception e) {
			//이벤트 발행 실패해도 메인 로직에 영향 없도록
			log.warn("이벤트 발행 실패하지만 메인 기능은 정상 처리됨: {}", e.getMessage());
		}
	}
}
