package pp.coinwash.machine.service.scheduler;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.service.UsingMachineService;
import pp.coinwash.machine.service.redis.MachineRedisService;

@ExtendWith(SpringExtension.class)
class MachineSchedulerServiceTest {

	@Mock
	private MachineRedisService machineRedisService;

	@Mock
	private UsingMachineService usingMachineService;

	@Mock
	private TaskScheduler taskScheduler;

	@Mock
	private ScheduledFuture<Object> scheduledFuture;

	@InjectMocks
	private MachineSchedulerService machineSchedulerService;

	private Machine testMachine;

	@BeforeEach
	void setUp() {
		testMachine = Machine.builder()
			.machineId(1L)
			.build();
	}

	@Test
	@DisplayName("기계 스케줄링이 정상적으로 등록되는지 테스트")
	void scheduleMachine_Success() {
		// Given
		LocalDateTime endTime = LocalDateTime.now().plusHours(1);

		// doReturn 사용으로 타입 문제 해결
		doReturn(scheduledFuture)
			.when(taskScheduler)
			.schedule(any(Runnable.class), any(Instant.class));

		// When
		machineSchedulerService.scheduleMachine(testMachine, endTime);

		// Then
		verify(taskScheduler, times(1)).schedule(any(Runnable.class), any(Instant.class));
	}

	@Test
	@DisplayName("기존 스케줄이 있을 때 새로운 스케줄로 교체되는지 테스트")
	void scheduleMachine_ReplaceExistingSchedule() {
		// Given
		// 기존 스케줄된 작업이 아직 실행 중인 상황을 시뮬레이션
		LocalDateTime endTime = LocalDateTime.now().plusHours(1);
		ScheduledFuture<?> oldFuture = mock(ScheduledFuture.class);
		when(oldFuture.isDone()).thenReturn(false);  // 아직 완료되지 않음

		// 기존 스케줄 등록
		machineSchedulerService.getScheduledTasks().put(testMachine.getMachineId(), oldFuture);

		doReturn(scheduledFuture)
			.when(taskScheduler)
			.schedule(any(Runnable.class), any(Instant.class));


		// When
		machineSchedulerService.scheduleMachine(testMachine, endTime);

		// Then
		verify(oldFuture).cancel(false); // 기존 스케줄이 취소되었는지 확인
		verify(taskScheduler).schedule(any(Runnable.class), any(Instant.class));
	}

	@Test
	@DisplayName("스케줄된 작업이 실행될 때 정상적으로 처리되는지 테스트")
	void scheduleMachine_TaskExecution() {
		// Given
		LocalDateTime endTime = LocalDateTime.now().plusHours(1);
		ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

		doReturn(scheduledFuture)
			.when(taskScheduler)
			.schedule(any(Runnable.class), any(Instant.class));

		// When
		machineSchedulerService.scheduleMachine(testMachine, endTime);

		// Then
		verify(taskScheduler).schedule(runnableCaptor.capture(), any(Instant.class));

		// 캡처된 Runnable 실행
		Runnable capturedTask = runnableCaptor.getValue();
		capturedTask.run();

		// 스케줄된 작업이 실행되었을 때의 동작 검증
		verify(machineRedisService).resetMachine(testMachine);
		verify(usingMachineService).resetStatus(testMachine.getMachineId());
	}

	@Test
	@DisplayName("스케줄 취소가 정상적으로 동작하는지 테스트")
	void cancelScheduledTask_Success() {
		// Given
		Long machineId = 1L;
		when(scheduledFuture.isDone()).thenReturn(false);

		// 스케줄 등록
		machineSchedulerService.getScheduledTasks().put(machineId, scheduledFuture);

		// When
		machineSchedulerService.cancelScheduledTask(machineId);

		// Then
		verify(scheduledFuture).cancel(false);
		assertThat(machineSchedulerService.getScheduledTasks()).doesNotContainKey(machineId);
	}

	@Test
	@DisplayName("이미 완료된 스케줄 취소 시 cancel이 호출되지 않는지 테스트")
	void cancelScheduledTask_AlreadyDone() {
		// Given
		Long machineId = 1L;
		when(scheduledFuture.isDone()).thenReturn(true);

		machineSchedulerService.getScheduledTasks().put(machineId, scheduledFuture);

		// When
		machineSchedulerService.cancelScheduledTask(machineId);

		// Then
		verify(scheduledFuture, never()).cancel(anyBoolean());
		assertThat(machineSchedulerService.getScheduledTasks()).doesNotContainKey(machineId);
	}

	@Test
	@DisplayName("존재하지 않는 스케줄 취소 시 예외가 발생하지 않는지 테스트")
	void cancelScheduledTask_NotExists() {
		// Given
		Long machineId = 999L;

		// When & Then
		assertThatCode(() -> machineSchedulerService.cancelScheduledTask(machineId))
			.doesNotThrowAnyException();
	}
}