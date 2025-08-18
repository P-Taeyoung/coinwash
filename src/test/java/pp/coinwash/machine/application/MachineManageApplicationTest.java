package pp.coinwash.machine.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.lettuce.core.RedisException;
import pp.coinwash.machine.domain.dto.MachineRedisDto;
import pp.coinwash.machine.domain.dto.MachineRegisterDto;
import pp.coinwash.machine.domain.dto.MachineResponseDto;
import pp.coinwash.machine.domain.dto.MachineUpdateDto;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.domain.type.MachineType;
import pp.coinwash.machine.domain.type.UsageStatus;
import pp.coinwash.machine.service.MachineManageService;
import pp.coinwash.machine.service.redis.MachineRedisService;

@ExtendWith(SpringExtension.class)
class MachineManageApplicationTest {

	@Mock
	MachineRedisService redisService;

	@Mock
	MachineManageService manageService;

	@InjectMocks
	MachineManageApplication application;


	private MachineRegisterDto registerDto;
	private Machine machine;
	private MachineUpdateDto updateDto;


	@BeforeEach
	void setUp() {
		registerDto = MachineRegisterDto.builder()
			.machineType(MachineType.WASHING)
			.build();

		machine = Machine.builder()
			.machineId(1L)
			.machineType(MachineType.WASHING)
			.usageStatus(UsageStatus.USABLE)
			.build();

		updateDto = MachineUpdateDto.builder()
			.machineId(1L)
			.usageStatus(UsageStatus.UNUSABLE)
			.build();
	}

	@Test
	@DisplayName("DB와 레디스 둘 다 저장 메서드가 잘 호출되는지 확인")
	void testRegisterMachines_Success() {
		// Given
		long laundryId = 1L;
		long ownerId = 1L;
		List<MachineRegisterDto> dtos = List.of(registerDto);
		List<Machine> machines = List.of(machine);
		when(manageService.registerMachines(dtos, laundryId, ownerId)).thenReturn(machines);

		// When
		application.registerMachines(dtos, laundryId, ownerId);

		// Then
		verify(manageService).registerMachines(dtos, laundryId, ownerId);
		verify(redisService, times(1)).saveMachineToRedis(any(Machine.class)); // Redis 저장 확인
	}

	@Test
	@DisplayName("기계 업데이트")
	void testUpdateMachine_Success() {
		// Given
		long ownerId = 1L;
		machine.updateOf(updateDto);
		when(manageService.updateMachine(updateDto, ownerId)).thenReturn(machine);

		// When
		application.updateMachine(updateDto, ownerId);

		// Then
		verify(manageService).updateMachine(updateDto, ownerId);
		verify(redisService).updateMachine(machine); // Redis 업데이트 확인
	}

	@Test
	@DisplayName("기계 삭제")
	void testDeleteMachine_Success() {
		// Given
		long machineId = 1L;
		long ownerId = 1L;

		// When
		application.deleteMachine(machineId, ownerId);

		// Then
		verify(manageService).deleteMachine(machineId, ownerId);
		verify(redisService).deleteMachine(machine); // Redis 삭제 확인
	}

}