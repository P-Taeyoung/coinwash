package pp.coinwash.machine.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pp.coinwash.history.domain.type.WashingCourse;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.domain.repository.MachineRepository;
import pp.coinwash.machine.domain.type.UsageStatus;
import pp.coinwash.point.application.PointHistoryApplication;
import pp.coinwash.point.domain.dto.PointHistoryRequestDto;

@ExtendWith(MockitoExtension.class)
class ReservingMachineServiceTest {

	@Mock
	private MachineRepository machineRepository;

	@Mock
	private PointHistoryApplication pointHistoryApplication;

	@InjectMocks
	private ReservingMachineService reservingMachineService;

	private Machine machine;
	private Machine cancelReserveMachine;

	@DisplayName("예약 성공")
	@Test
	void reserveMachine() {
		//given
		long machineId = 1;
		long customerId = 1;
		machine = Machine.builder()
			.machineId(1)
			.usageStatus(UsageStatus.USABLE)
			.endTime(null)
			.customerId(null)
			.build();

		when(machineRepository.findUsableMachineByMachineId(machineId))
			.thenReturn(Optional.ofNullable(machine));

		//when
		reservingMachineService.reserveMachine(machineId, customerId);

		//then
		verify(pointHistoryApplication, times(1))
			.usePoints(PointHistoryRequestDto.usePoint(customerId,
				100));

		verify(machineRepository, times(1)).findUsableMachineByMachineId(machineId);
		assertEquals(customerId, machine.getCustomerId());
		assertEquals(LocalDateTime.now().plusMinutes(15).truncatedTo(ChronoUnit.SECONDS)
			, machine.getEndTime().truncatedTo(ChronoUnit.SECONDS));
	}

	@DisplayName("예약 취소")
	@Test
	void cancelReserveMachine() {
		//given
		long machineId = 1;
		long customerId = 1;
		machine = Machine.builder()
			.customerId(1L)
			.build();
		when(machineRepository.findUsableMachineByMachineId(machineId))
			.thenReturn(Optional.ofNullable(machine));

		//when
		reservingMachineService.cancelReserveMachine(machineId, customerId);

		//then
		verify(machineRepository, times(1)).findUsableMachineByMachineId(machineId);
		assertNull(machine.getCustomerId());
		assertNull(machine.getEndTime());
		assertEquals(UsageStatus.USABLE, machine.getUsageStatus());
	}

	@DisplayName("예약 취소 권한 없음")
	@Test
	void unableToCancelReserveMachine() {
		//given
		long machineId = 1;
		long customerId = 1;
		machine = Machine.builder()
			.customerId(2L)
			.build();
		when(machineRepository.findUsableMachineByMachineId(machineId))
			.thenReturn(Optional.ofNullable(machine));

		//then
		RuntimeException exception =
			assertThrows(RuntimeException.class,
				() -> reservingMachineService.cancelReserveMachine(machineId, customerId));

		assertEquals("예약 취소 권한이 없습니다.", exception.getMessage());
	}
}