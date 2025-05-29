package pp.coinwash.machine.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pp.coinwash.history.domain.type.DryingCourse;
import pp.coinwash.history.domain.type.WashingCourse;
import pp.coinwash.laundry.domain.entity.Laundry;
import pp.coinwash.machine.domain.dto.UsingDryingDto;
import pp.coinwash.machine.domain.dto.UsingWashingDto;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.domain.repository.MachineRepository;
import pp.coinwash.machine.domain.type.MachineType;
import pp.coinwash.machine.domain.type.UsageStatus;

@ExtendWith(MockitoExtension.class)
class UsingMachineServiceTest {

	@Mock
	private MachineRepository machineRepository;

	@InjectMocks
	private UsingMachineService usingMachineService;

	private Machine washingMachine;
	private Machine dryingMachine;
	private UsingWashingDto washingDto;
	private UsingDryingDto dryingDto;
	private Laundry laundry;

	@BeforeEach
	void setUp() {
		laundry  = Laundry.builder()
			.laundryId(1)
			.build();

		washingMachine = Machine.builder()
			.machineId(1)
			.laundry(laundry)
			.machineType(MachineType.WASHING)
			.usageStatus(UsageStatus.USABLE)
			.endTime(null)
			.customerId(null)
			.build();

		dryingMachine = Machine.builder()
			.machineId(1)
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
			.machineId(1)
			.course(WashingCourse.WASHING_A_COURSE)
			.build();
		when(machineRepository.findMachineByMachineId(1, MachineType.WASHING))
			.thenReturn(Optional.ofNullable(washingMachine));

		//when
		usingMachineService.useWashing(customerId, washingDto);

		//then
		verify(machineRepository, times(1)).findMachineByMachineId(1, MachineType.WASHING);
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
		when(machineRepository.findMachineByMachineId(1, MachineType.DRYING))
			.thenReturn(Optional.ofNullable(dryingMachine));

		//when
		usingMachineService.useDrying(customerId, dryingDto);

		//then
		verify(machineRepository, times(1)).findMachineByMachineId(1, MachineType.DRYING);
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
			.machineId(1)
			.course(WashingCourse.WASHING_A_COURSE)
			.build();

		washingMachine = Machine.builder()
			.machineId(1)
			.laundry(laundry)
			.machineType(MachineType.WASHING)
			.usageStatus(UsageStatus.USING)
			.build();

		when(machineRepository.findMachineByMachineId(1, MachineType.WASHING))
			.thenReturn(Optional.ofNullable(washingMachine));

		//then
		RuntimeException exception = assertThrows(RuntimeException.class,
			() -> usingMachineService.useWashing(customerId, washingDto));

		assertEquals("현재 사용할 수 없는 기계입니다.", exception.getMessage());
	}

	@DisplayName("사용할 수 없는 기계 예외")
	@Test
	void alreadyReservedMachine() {
		//given
		long customerId = 1;
		washingDto = UsingWashingDto.builder()
			.machineId(1)
			.course(WashingCourse.WASHING_A_COURSE)
			.build();

		washingMachine = Machine.builder()
			.machineId(1)
			.laundry(laundry)
			.machineType(MachineType.WASHING)
			.usageStatus(UsageStatus.RESERVING)
			.customerId(2L)
			.build();

		when(machineRepository.findMachineByMachineId(1, MachineType.WASHING))
			.thenReturn(Optional.ofNullable(washingMachine));

		//then
		RuntimeException exception = assertThrows(RuntimeException.class,
			() -> usingMachineService.useWashing(customerId, washingDto));

		assertEquals("예약자가 아닙니다.", exception.getMessage());
	}
}