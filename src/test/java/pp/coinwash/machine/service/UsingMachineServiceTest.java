package pp.coinwash.machine.service;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import pp.coinwash.history.domain.dto.HistoryRequestDto;
import pp.coinwash.history.domain.repository.HistoryRepository;
import pp.coinwash.history.domain.type.DryingCourse;
import pp.coinwash.history.domain.type.WashingCourse;
import pp.coinwash.history.event.HistoryEvent;
import pp.coinwash.history.service.HistoryService;
import pp.coinwash.laundry.domain.entity.Laundry;
import pp.coinwash.machine.domain.dto.UsingDryingDto;
import pp.coinwash.machine.domain.dto.UsingWashingDto;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.domain.repository.MachineRepository;
import pp.coinwash.machine.domain.type.MachineType;
import pp.coinwash.machine.domain.type.UsageStatus;
import pp.coinwash.machine.event.MachineEvent;
import pp.coinwash.point.application.PointHistoryApplication;
import pp.coinwash.point.domain.dto.PointHistoryRequestDto;

@ExtendWith(MockitoExtension.class)
class UsingMachineServiceTest {

	@Mock
	private MachineRepository machineRepository;

	@Mock
	private PointHistoryApplication pointHistoryApplication;

	@Mock
	ApplicationEventPublisher eventPublisher;

	@Mock
	HistoryService historyService;

	@Mock
	private HistoryRepository historyRepository;


	@InjectMocks
	private UsingMachineService usingMachineService;

	private Machine washingMachine;
	private Machine dryingMachine;
	private UsingWashingDto washingDto;
	private UsingDryingDto dryingDto;
	private Laundry laundry;

	@BeforeEach
	void setUp() {
		laundry = Laundry.builder()
			.laundryId(1)
			.build();

		washingMachine = Machine.builder()
			.machineId(1L)
			.laundry(laundry)
			.machineType(MachineType.WASHING)
			.usageStatus(UsageStatus.USABLE)
			.endTime(null)
			.customerId(null)
			.build();

		dryingMachine = Machine.builder()
			.machineId(1L)
			.laundry(laundry)
			.machineType(MachineType.DRYING)
			.usageStatus(UsageStatus.USABLE)
			.endTime(null)
			.customerId(null)
			.build();

	}

	@DisplayName("세탁기 사용 성공")
	@Test
	void useWashing() {
		//given
		long customerId = 1;
		washingDto = UsingWashingDto.builder()
			.machineId(1L)
			.course(WashingCourse.WASHING_A_COURSE)
			.build();
		when(machineRepository.findUsableMachineWithLock(1, MachineType.WASHING))
			.thenReturn(Optional.ofNullable(washingMachine));

		//when
		usingMachineService.useWashing(customerId, washingDto);

		//then
		verify(pointHistoryApplication, times(1))
			.usePoints(PointHistoryRequestDto.usePoint(customerId,
				WashingCourse.WASHING_A_COURSE.getFee()));

		verify(machineRepository, times(1)).findUsableMachineWithLock(1, MachineType.WASHING);
		assertEquals(customerId, washingMachine.getCustomerId());
		assertEquals(UsageStatus.USING, washingMachine.getUsageStatus());
		assertEquals(LocalDateTime.now()
				.plusMinutes(WashingCourse.WASHING_A_COURSE.getCourseTime())
				.truncatedTo(ChronoUnit.SECONDS)
			, washingMachine.getEndTime().truncatedTo(ChronoUnit.SECONDS));

	}

	@DisplayName("건조기 사용 성공")
	@Test
	void useDrying() {
		//given
		long customerId = 1;
		dryingDto = UsingDryingDto.builder()
			.machineId(1)
			.course(DryingCourse.DRYING_A_COURSE)
			.build();
		when(machineRepository.findUsableMachineWithLock(1, MachineType.DRYING))
			.thenReturn(Optional.ofNullable(dryingMachine));

		//when
		usingMachineService.useDrying(customerId, dryingDto);

		//then
		verify(pointHistoryApplication, times(1))
			.usePoints(PointHistoryRequestDto.usePoint(customerId,
				DryingCourse.DRYING_A_COURSE.getFee()));

		verify(machineRepository, times(1)).findUsableMachineWithLock(1, MachineType.DRYING);
		assertEquals(customerId, dryingMachine.getCustomerId());
		assertEquals(UsageStatus.USING, dryingMachine.getUsageStatus());
		assertEquals(LocalDateTime.now()
				.plusMinutes(DryingCourse.DRYING_A_COURSE.getCourseTime())
				.truncatedTo(ChronoUnit.SECONDS)
			, dryingMachine.getEndTime().truncatedTo(ChronoUnit.SECONDS));
	}

	@DisplayName("기계 상태 초기화 성공")
	@Test
	void resetStatus() {
		//given
		long machineId = 1;
		when(machineRepository.findById(machineId))
			.thenReturn(Optional.ofNullable(washingMachine));

		//when
		usingMachineService.resetStatus(machineId);

		//then
		verify(machineRepository, times(1)).findById(machineId);
		assertEquals(UsageStatus.USABLE, washingMachine.getUsageStatus());
		assertNull(washingMachine.getCustomerId());
		assertNull(washingMachine.getEndTime());
	}

	@DisplayName("사용할 수 없는 기계 예외")
	@Test
	void unusableMachine() {
		//given
		long customerId = 1;
		washingDto = UsingWashingDto.builder()
			.machineId(1L)
			.course(WashingCourse.WASHING_A_COURSE)
			.build();

		washingMachine = Machine.builder()
			.machineId(1L)
			.laundry(laundry)
			.machineType(MachineType.WASHING)
			.usageStatus(UsageStatus.USING)
			.endTime(LocalDateTime.now().plusMinutes(WashingCourse.WASHING_A_COURSE.getCourseTime()))
			.build();

		when(machineRepository.findUsableMachineWithLock(1, MachineType.WASHING))
			.thenReturn(Optional.ofNullable(washingMachine));

		//then
		RuntimeException exception = assertThrows(RuntimeException.class,
			() -> usingMachineService.useWashing(customerId, washingDto));

		assertEquals("이미 사용중인 기계입니다.", exception.getMessage());
	}

	@DisplayName("이미 예약되어 있는 기계")
	@Test
	void alreadyReservedMachine() {
		//given
		long customerId = 1;
		washingDto = UsingWashingDto.builder()
			.machineId(1L)
			.course(WashingCourse.WASHING_A_COURSE)
			.build();

		washingMachine = Machine.builder()
			.machineId(1L)
			.laundry(laundry)
			.machineType(MachineType.WASHING)
			.usageStatus(UsageStatus.RESERVING)
			.customerId(2L)
			.endTime(LocalDateTime.now().plusMinutes(15))
			.build();

		when(machineRepository.findUsableMachineWithLock(1, MachineType.WASHING))
			.thenReturn(Optional.ofNullable(washingMachine));

		//then
		RuntimeException exception = assertThrows(RuntimeException.class,
			() -> usingMachineService.useWashing(customerId, washingDto));

		assertEquals("예약자가 아닙니다.", exception.getMessage());
	}

	@DisplayName("종료 시간이 넘어간 기계는 사용 가능")
	@Test
	void usingMachineWithReset() {
		//Given
		long customerId = 1;
		washingDto = UsingWashingDto.builder()
			.machineId(1L)
			.course(WashingCourse.WASHING_A_COURSE)
			.build();

		Machine machine = Machine.builder()
			.machineId(1L)
			.usageStatus(UsageStatus.RESERVING)
			.endTime(LocalDateTime.now().minusMinutes(1))
			.customerId(2L)
			.build();

		when(machineRepository.findUsableMachineWithLock(1, MachineType.WASHING))
			.thenReturn(Optional.ofNullable(machine));

		//When
		usingMachineService.useWashing(customerId, washingDto);

		//Then
		verify(machineRepository, times(1)).findUsableMachineWithLock(1, MachineType.WASHING);
		assertEquals(customerId, machine.getCustomerId());
		assertEquals(UsageStatus.USING, machine.getUsageStatus());
	}

	@DisplayName("세탁기 사용 시 historyEvent 발행")
	@Test
	void createHistoryEvent() {
		//given
		long customerId = 1;
		washingDto = UsingWashingDto.builder()
			.machineId(1L)
			.course(WashingCourse.WASHING_A_COURSE)
			.build();

		when(machineRepository.findUsableMachineWithLock(1, MachineType.WASHING))
			.thenReturn(Optional.ofNullable(washingMachine));

		//when
		usingMachineService.useWashing(customerId, washingDto);

		//then
		verify(eventPublisher, times(1))
			.publishEvent(new HistoryEvent(HistoryRequestDto.createWashingHistory(
				customerId, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), WashingCourse.WASHING_A_COURSE
			), washingMachine));

		ArgumentCaptor<HistoryEvent> eventCaptor = ArgumentCaptor.forClass(HistoryEvent.class);
		verify(eventPublisher).publishEvent(eventCaptor.capture());

		HistoryEvent capturedEvent = eventCaptor.getValue();

		assertThat(capturedEvent.machine()).isEqualTo(washingMachine);
	}

	@DisplayName("세탁기 사용 시 MachineEvent 발행")
	@Test
	void createMachineEvent() {
		//given
		long customerId = 1;
		washingDto = UsingWashingDto.builder()
			.machineId(1L)
			.course(WashingCourse.WASHING_A_COURSE)
			.build();

		when(machineRepository.findUsableMachineWithLock(1, MachineType.WASHING))
			.thenReturn(Optional.ofNullable(washingMachine));

		//when
		usingMachineService.useWashing(customerId, washingDto);

		//then
		verify(eventPublisher, times(1))
			.publishEvent(MachineEvent.usingMachineEvent(washingMachine));

		ArgumentCaptor<HistoryEvent> eventCaptor = ArgumentCaptor.forClass(HistoryEvent.class);
		verify(eventPublisher).publishEvent(eventCaptor.capture());

		HistoryEvent capturedEvent = eventCaptor.getValue();

		assertThat(capturedEvent.machine()).isEqualTo(washingMachine);
	}


	@DisplayName("historyEvent 발행 실패에도 기존 트랜잭션에 영향 X")
	@Test
	void failedHistoryEvent() {
		long customerId = 1;
		washingDto = UsingWashingDto.builder()
			.machineId(1L)
			.course(WashingCourse.WASHING_A_COURSE)
			.build();

		when(machineRepository.findUsableMachineWithLock(1, MachineType.WASHING))
			.thenReturn(Optional.ofNullable(washingMachine));

		// 🎯 이벤트 리스너에서 예외 발생하도록 설정
		lenient().doThrow(RuntimeException.class)
			.when(eventPublisher).publishEvent(any(HistoryEvent.class));

		//when & then
		assertThatCode(() -> {
			usingMachineService.useWashing(customerId, washingDto);
		}).doesNotThrowAnyException(); // 메인 로직은 성공해야 함

		//then
		verify(machineRepository, times(1)).findUsableMachineWithLock(1, MachineType.WASHING);
		assertEquals(customerId, washingMachine.getCustomerId());
		assertEquals(UsageStatus.USING, washingMachine.getUsageStatus());
	}

}