package pp.coinwash.machine.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pp.coinwash.laundry.domain.entity.Laundry;
import pp.coinwash.laundry.domain.repository.LaundryRepository;
import pp.coinwash.machine.domain.dto.MachineRegisterDto;
import pp.coinwash.machine.domain.dto.MachineResponseDto;
import pp.coinwash.machine.domain.dto.MachineUpdateDto;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.domain.repository.MachineRepository;
import pp.coinwash.machine.domain.type.MachineType;
import pp.coinwash.machine.domain.type.UsageStatus;

@ExtendWith(MockitoExtension.class)
class MachineManageServiceTest {

	@Mock
	MachineRepository machineRepository;

	@Mock
	LaundryRepository laundryRepository;

	@InjectMocks
	MachineManageService machineManageService;

	private Laundry laundry;
	private Machine machine1;
	private Machine machine2;
	private MachineRegisterDto registerDto1;
	private MachineRegisterDto registerDto2;
	private MachineUpdateDto updateDto;

	@BeforeEach
	void setUp() {
		laundry = Laundry.builder()
			.laundryId(1)
			.ownerId(1)
			.deletedAt(null)
			.build();

		machine1 = Machine.builder()
			.machineId(1)
			.laundry(laundry)
			.machineType(MachineType.DRYING)
			.usageStatus(UsageStatus.USABLE)
			.build();

		machine2 = Machine.builder()
			.machineId(1)
			.laundry(laundry)
			.machineType(MachineType.WASHING)
			.usageStatus(UsageStatus.UNUSABLE)
			.build();

		registerDto1 = MachineRegisterDto.builder()
			.machineType(MachineType.WASHING)
			.notes("이상무")
			.build();

		registerDto2 = MachineRegisterDto.builder()
			.machineType(MachineType.DRYING)
			.notes("이상무")
			.build();

		updateDto = MachineUpdateDto.builder()
			.machineId(1)
			.notes("모터 고장")
			.build();
	}


	@DisplayName("기계 정보 등록")
	@Test
	void registerMachines() {
		//given
		long ownerId = 1L;
		long laundryId = 1L;
		when(laundryRepository.findByLaundryIdAndOwnerIdAndDeletedAtIsNull(laundryId, ownerId))
			.thenReturn(Optional.ofNullable(laundry));

		//when
		machineManageService.registerMachines(List.of(registerDto1, registerDto2), laundryId, ownerId);

		//then
		ArgumentCaptor<Machine> captor = ArgumentCaptor.forClass(Machine.class);
		verify(machineRepository, times(2)).save(captor.capture());

		List<Machine> machines = captor.getAllValues();
		assertEquals(2, machines.size());
		assertEquals(MachineType.WASHING, machines.get(0).getMachineType());
		assertEquals(MachineType.DRYING, machines.get(1).getMachineType());
	}

	@DisplayName("특정 점주 매장의 기계 정보 조회")
	@Test
	void getMachinesByLaundryId() {
		//given
		long ownerId = 1L;
		long laundryId = 1L;
		when(laundryRepository.existsByLaundryIdAndOwnerIdAndDeletedAtIsNull(laundryId, ownerId))
			.thenReturn(true);
		when(machineRepository.findByLaundryLaundryIdAndDeletedAtIsNull(laundryId))
			.thenReturn(List.of(machine1, machine2));

		//when
		List<MachineResponseDto> machines = machineManageService.getMachinesByLaundryId(laundryId, ownerId);

		//then
        verify(machineRepository, times(1)).findByLaundryLaundryIdAndDeletedAtIsNull(laundryId);
		assertEquals(2, machines.size());
		assertEquals(MachineResponseDto.from(machine1), machines.get(0));
		assertEquals(MachineResponseDto.from(machine2), machines.get(1));
	}

	@DisplayName("기계 정보 수정")
	@Test
	void updateMachine() {
		//given
		long ownerId = 1L;
		when(machineRepository.findByMachineIdAndLaundryOwnerId(updateDto.machineId(), ownerId))
			.thenReturn(Optional.ofNullable(machine1));

		//when
		machineManageService.updateMachine(updateDto, ownerId);

		//then
		assertEquals(updateDto.usageStatus(), machine1.getUsageStatus());
		assertEquals(updateDto.notes(), machine1.getNotes());

	}

	@DisplayName("기계 정보 삭제")
	@Test
	void deleteMachine() {
		//given
		long ownerId = 1L;
		when(machineRepository.findByMachineIdAndLaundryOwnerId(updateDto.machineId(), ownerId))
			.thenReturn(Optional.ofNullable(machine1));

		//when
		machineManageService.deleteMachine(machine1.getMachineId(), ownerId);

		//then
		assertNotNull(machine1.getDeletedAt());
	}
}