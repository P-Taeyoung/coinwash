package pp.coinwash.machine.service.scheduler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.domain.type.MachineType;
import pp.coinwash.machine.event.MachineEvent;
import pp.coinwash.notification.domain.dto.NotificationRequestDto;
import pp.coinwash.notification.service.SseEmitterService;

@Component
@RequiredArgsConstructor
@Slf4j
public class MachineSchedulerService {

	private final SseEmitterService sseEmitterService;

	private final TaskScheduler taskScheduler;

	//테스트 시 get 메서드 활용을 위해 추가
	@Getter
	private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

	@Async("machineTaskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleMachineEvent(MachineEvent event) {

		if (event.scheduleType() == MachineEvent.ScheduleType.USING) {

			scheduleUsingMachine(event.machine(), event.endTime());
		} else if (event.scheduleType() == MachineEvent.ScheduleType.RESERVING) {

			scheduleReservingMachine(event.machine(), event.endTime());
		} else {

			cancelScheduledTask(event.machine().getMachineId());
		}
	}

	public void scheduleUsingMachine(Machine machine, LocalDateTime endTime) {
		//기존 스케줄이 있었다면 해당 스케줄 삭제 && 스케줄 시간 세팅
		Instant instant = setScheduler(machine.getMachineId(), endTime);

		ScheduledFuture<?> future = taskScheduler.schedule(() -> {

			log.info("기계 (ID : {}) 종료 예정 알림 전송", machine.getMachineId());

			if (machine.getMachineType() == MachineType.WASHING) {

				sseEmitterService.sendToCustomer(
					NotificationRequestDto.createNotificationRequestDto(
						machine.getCustomerId(), "세탁이 곧 완료됩니다.", endTime
					)
				);
			} else {

				sseEmitterService.sendToCustomer(
					NotificationRequestDto.createNotificationRequestDto(
						machine.getCustomerId(), "건조가 곧 완료됩니다.", endTime
					)
				);
			}

			scheduledTasks.remove(machine.getMachineId());

		}, instant);

		scheduledTasks.put(machine.getMachineId(), future);
		log.info("기계 종료 전 알림 스케줄링 완료: machineId={}, endTime={}", machine.getMachineId(), endTime);
	}

	public void scheduleReservingMachine(Machine machine, LocalDateTime endTime) {
		Instant instant = setScheduler(machine.getMachineId(), endTime);

		ScheduledFuture<?> future = taskScheduler.schedule(() -> {

			log.info("기계 (ID : {}) 예약 종료 예정 알림 전송", machine.getMachineId());

			sseEmitterService.sendToCustomer(
				NotificationRequestDto.createNotificationRequestDto(
					machine.getCustomerId(), "예약 시간이 곧 종료됩니다.", endTime
				)
			);

			scheduledTasks.remove(machine.getMachineId());

		}, instant);

		scheduledTasks.put(machine.getMachineId(), future);
		log.info("예약 종료 전 알림 스케줄링 완료: machineId={}, endTime={}", machine.getMachineId(), endTime);
	}

	public void cancelScheduledTask(Long machineId) {
		ScheduledFuture<?> future = scheduledTasks.remove(machineId);
		if (future != null && !future.isDone()) {
			future.cancel(false);
		}
	}

	private Instant setScheduler(Long machineId, LocalDateTime endTime) {

		cancelScheduledTask(machineId);

		endTime = endTime.minusMinutes(5);

		return endTime.atZone(ZoneId.systemDefault()).toInstant();
	}
}
