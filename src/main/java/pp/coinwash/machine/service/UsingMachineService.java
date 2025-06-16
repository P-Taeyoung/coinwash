package pp.coinwash.machine.service;

import static pp.coinwash.machine.domain.type.MachineType.*;
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
import pp.coinwash.machine.domain.dto.UsingDryingDto;
import pp.coinwash.machine.domain.dto.UsingWashingDto;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.domain.repository.MachineRepository;
import pp.coinwash.machine.domain.type.MachineType;
import pp.coinwash.machine.domain.type.UsageStatus;
import pp.coinwash.machine.event.MachineEvent;
import pp.coinwash.machine.service.scheduler.MachineSchedulerService;
import pp.coinwash.point.application.PointHistoryApplication;
import pp.coinwash.point.domain.dto.PointHistoryRequestDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsingMachineService {

	private final MachineRepository machineRepository;

	private final PointHistoryApplication pointHistoryApplication;

	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public Machine useWashing(long customerId, UsingWashingDto usingWashingDto) {

		Machine machine = verifyUsableMachine(
			usingWashingDto.machineId(), customerId, WASHING);

		pointHistoryApplication.usePoints(
			PointHistoryRequestDto.usePoint(customerId,
				usingWashingDto.course().getFee()));

		machine.useWashing(customerId, usingWashingDto.course());

		publishHistoryEventSafely(
			HistoryRequestDto.createWashingHistory(
				customerId,
				LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
				usingWashingDto.course()),
			machine);

		publishSchedulerEventSafely(machine);

		return machine;
	}

	@Transactional
	public Machine useDrying(long customerId, UsingDryingDto usingDryingDto) {

		Machine machine = verifyUsableMachine(
			usingDryingDto.machineId(), customerId, DRYING);

		pointHistoryApplication.usePoints(
			PointHistoryRequestDto.usePoint(customerId,
				usingDryingDto.course().getFee()));

		machine.useDrying(customerId, usingDryingDto.course());

		publishHistoryEventSafely(
			HistoryRequestDto.createDryingHistory(
				customerId,
				LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
				usingDryingDto.course()),
			machine);

		publishSchedulerEventSafely(machine);

		return machine;
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

		if (machine.getUsageStatus() == UsageStatus.UNUSABLE) {
			throw new RuntimeException("현재 사용할 수 없는 기계입니다.");
		}

		if (machine.getEndTime() != null && machine.getEndTime().isAfter(LocalDateTime.now())) {

			if (machine.getUsageStatus() == UsageStatus.USING) {
				throw new RuntimeException("이미 사용중인 기계입니다.");
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
		}

		return machine;
	}

	private void publishHistoryEventSafely(HistoryRequestDto dto, Machine machine) {
		try {
			eventPublisher.publishEvent(HistoryEvent.of(dto, machine));
		} catch (Exception e) {
			//이벤트 발행 실패해도 메인 로직에 영향 없도록
			log.warn("이벤트 발행 실패하지만 메인 기능은 정상 처리됨: {}", e.getMessage());
		}
	}

	private void publishSchedulerEventSafely(Machine machine) {
		try {
			eventPublisher.publishEvent(usingMachineEvent(machine));

		} catch (Exception e) {
			//이벤트 발행 실패해도 메인 로직에 영향 없도록
			log.warn("이벤트 발행 실패하지만 메인 기능은 정상 처리됨: {}", e.getMessage());
		}
	}

}
