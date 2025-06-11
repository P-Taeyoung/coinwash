package pp.coinwash.machine.application;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.lettuce.core.RedisException;
import pp.coinwash.history.domain.type.DryingCourse;
import pp.coinwash.history.domain.type.WashingCourse;
import pp.coinwash.machine.domain.dto.UsingDryingDto;
import pp.coinwash.machine.domain.dto.UsingWashingDto;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.domain.type.UsageStatus;
import pp.coinwash.machine.service.ReservingMachineService;
import pp.coinwash.machine.service.UsingMachineService;
import pp.coinwash.machine.service.redis.MachineRedisService;
import pp.coinwash.machine.service.scheduler.MachineSchedulerService;

@ExtendWith(SpringExtension.class)
class MachineApplicationTest {

	@Mock
	private MachineRedisService redisService;

	@Mock
	private UsingMachineService usingService;

	@Mock
	private ReservingMachineService reservingService;

	@InjectMocks
	private MachineApplication machineApplication;

	private Machine mockMachine;
	private UsingWashingDto usingWashingDto;
	private UsingDryingDto usingDryingDto;

	@BeforeEach
	void setUp() {
		mockMachine = Machine.builder()
			.machineId(1L)
			.usageStatus(UsageStatus.USABLE)
			.build();

		usingWashingDto = UsingWashingDto.builder()
			.machineId(1L)
			.course(WashingCourse.WASHING_A_COURSE)
			.build();

		usingDryingDto = UsingDryingDto.builder()
			.machineId(2L)
			.course(DryingCourse.DRYING_A_COURSE)
			.build();
	}

	@Test
	@DisplayName("세탁기 사용 성공")
	void useWashing_Success() {
		// given
		long customerId = 100L;
		when(usingService.useWashing(customerId, usingWashingDto))
			.thenReturn(mockMachine);

		// when
		machineApplication.useWashing(customerId, usingWashingDto);

		// then
		verify(usingService).useWashing(customerId, usingWashingDto);
		verify(redisService).useMachine(mockMachine);
	}

	@Test
	@DisplayName("세탁기 사용 시 Redis 예외 발생")
	void useWashing_RedisException() {
		// given
		long customerId = 100L;
		when(usingService.useWashing(customerId, usingWashingDto))
			.thenReturn(mockMachine);
		doThrow(new RedisException("Redis 연결 실패"))
			.when(redisService).useMachine(mockMachine);

		// when & then
		RuntimeException exception = assertThrows(RuntimeException.class,
			() -> machineApplication.useWashing(customerId, usingWashingDto));

		assertThat(exception.getMessage()).isEqualTo("세탁기 사용에 실패했습니다");
		assertThat(exception.getCause()).isInstanceOf(RedisException.class);

		verify(usingService).useWashing(customerId, usingWashingDto);
		verify(redisService).useMachine(mockMachine);
	}

	@Test
	@DisplayName("세탁기 사용 시 DB 작업 예외 발생")
	void useWashing_DatabaseException() {
		// given
		long customerId = 100L;
		when(usingService.useWashing(customerId, usingWashingDto))
			.thenThrow(new IllegalStateException("기계가 이미 사용 중입니다"));

		// when & then
		IllegalStateException exception = assertThrows(IllegalStateException.class,
			() -> machineApplication.useWashing(customerId, usingWashingDto));

		assertThat(exception.getMessage()).isEqualTo("기계가 이미 사용 중입니다");

		verify(usingService).useWashing(customerId, usingWashingDto);
		verify(redisService, never()).useMachine(any());
	}

	@Test
	@DisplayName("건조기 사용 성공")
	void useDrying_Success() {
		// given
		long customerId = 100L;
		when(usingService.useDrying(customerId, usingDryingDto))
			.thenReturn(mockMachine);

		// when
		machineApplication.useDrying(customerId, usingDryingDto);

		// then
		verify(usingService).useDrying(customerId, usingDryingDto);
		verify(redisService).useMachine(mockMachine);
	}

	@Test
	@DisplayName("건조기 사용 시 Redis 예외 발생")
	void useDrying_RedisException() {
		// given
		long customerId = 100L;
		when(usingService.useDrying(customerId, usingDryingDto))
			.thenReturn(mockMachine);
		doThrow(new RedisException("Redis 연결 실패"))
			.when(redisService).useMachine(mockMachine);

		// when & then
		RuntimeException exception = assertThrows(RuntimeException.class,
			() -> machineApplication.useDrying(customerId, usingDryingDto));

		assertThat(exception.getMessage()).isEqualTo("건조기 사용에 실패했습니다");
		assertThat(exception.getCause()).isInstanceOf(RedisException.class);
	}

	@Test
	@DisplayName("기계 예약 성공")
	void reserveMachine_Success() {
		// given
		long machineId = 1L;
		long customerId = 100L;
		when(reservingService.reserveMachine(machineId, customerId))
			.thenReturn(mockMachine);

		// when
		machineApplication.reserveMachine(machineId, customerId);

		// then
		verify(reservingService).reserveMachine(machineId, customerId);
		verify(redisService).reserveMachine(mockMachine);
	}

	@Test
	@DisplayName("기계 예약 시 Redis 예외 발생")
	void reserveMachine_RedisException() {
		// given
		long machineId = 1L;
		long customerId = 100L;
		when(reservingService.reserveMachine(machineId, customerId))
			.thenReturn(mockMachine);
		doThrow(new RedisException("Redis 연결 실패"))
			.when(redisService).reserveMachine(mockMachine);

		// when & then
		RuntimeException exception = assertThrows(RuntimeException.class,
			() -> machineApplication.reserveMachine(machineId, customerId));

		assertThat(exception.getMessage()).isEqualTo("기계 예약에 실패했습니다");
		assertThat(exception.getCause()).isInstanceOf(RedisException.class);
	}

	@Test
	@DisplayName("기계 예약 취소 성공")
	void cancelReservingMachine_Success() {
		// given
		long machineId = 1L;
		long customerId = 100L;
		when(reservingService.cancelReserveMachine(machineId, customerId))
			.thenReturn(mockMachine);

		// when
		machineApplication.cancelReservingMachine(machineId, customerId);

		// then
		verify(reservingService).cancelReserveMachine(machineId, customerId);
		verify(redisService).resetMachine(mockMachine);
	}

	@Test
	@DisplayName("기계 예약 취소 시 Redis 예외 발생")
	void cancelReservingMachine_RedisException() {
		// given
		long machineId = 1L;
		long customerId = 100L;
		when(reservingService.cancelReserveMachine(machineId, customerId))
			.thenReturn(mockMachine);
		doThrow(new RedisException("Redis 연결 실패"))
			.when(redisService).resetMachine(mockMachine);

		// when & then
		RuntimeException exception = assertThrows(RuntimeException.class,
			() -> machineApplication.cancelReservingMachine(machineId, customerId));

		assertThat(exception.getMessage()).isEqualTo("기계 예약 취소에 실패했습니다");
		assertThat(exception.getCause()).isInstanceOf(RedisException.class);
	}

	@Test
	@DisplayName("DB 작업이 Redis 작업보다 먼저 실행되는지 확인")
	void executionOrder_DbBeforeRedis() {
		// given
		long customerId = 100L;
		InOrder inOrder = inOrder(usingService, redisService);
		when(usingService.useWashing(customerId, usingWashingDto))
			.thenReturn(mockMachine);

		// when
		machineApplication.useWashing(customerId, usingWashingDto);

		// then
		inOrder.verify(usingService).useWashing(customerId, usingWashingDto);
		inOrder.verify(redisService).useMachine(mockMachine);
	}

}