package pp.coinwash.machine.service.scheduler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.service.UsingMachineService;
import pp.coinwash.machine.service.redis.MachineRedisService;

@Component
@RequiredArgsConstructor
@Slf4j
public class MachineSchedulerService {

	private final MachineRedisService machineRedisService;
	private final UsingMachineService usingMachineService;
	private final TaskScheduler taskScheduler;

	//테스트 시 get 메서드 활용을 위해 추가
	@Getter
	private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

	public void scheduleMachine(Machine machine, LocalDateTime endTime) {
		//기존 스케줄이 있었다면 해당 스케줄 삭제
		cancelScheduledTask(machine.getMachineId());

		Instant instant = endTime.atZone(ZoneId.systemDefault()).toInstant();

		ScheduledFuture<?> future = taskScheduler.schedule(() -> {

			log.info("사용 가능한 상태로 변경된 기계 ID: {}", machine.getMachineId());

			machineRedisService.resetMachine(machine);
			usingMachineService.resetStatus(machine.getMachineId());

			scheduledTasks.remove(machine.getMachineId());

		}, instant);

		scheduledTasks.put(machine.getMachineId(), future);
		log.info("기계 스케줄링 완료: machineId={}, endTime={}", machine.getMachineId(), endTime);
	}

	public void cancelScheduledTask(Long machineId) {
		ScheduledFuture<?> future = scheduledTasks.remove(machineId);
		if (future != null && !future.isDone()) {
			future.cancel(false);
		}
	}
}
