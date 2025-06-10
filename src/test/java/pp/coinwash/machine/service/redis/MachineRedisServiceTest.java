package pp.coinwash.machine.service.redis;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import pp.coinwash.laundry.domain.entity.Laundry;
import pp.coinwash.machine.domain.dto.MachineRedisDto;
import pp.coinwash.machine.domain.dto.MachineUpdateDto;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.domain.repository.MachineRepository;
import pp.coinwash.machine.domain.type.MachineType;
import pp.coinwash.machine.domain.type.UsageStatus;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MachineRedisServiceTest {

	@Autowired
	private MachineRedisService machineRedisService;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@MockitoBean
	private MachineRepository machineRepository;

	private Machine testMachine;
	private Laundry testLaundry;

	@BeforeEach
	void setUp() {
		// Redis 데이터 초기화
		redisTemplate.getConnectionFactory().getConnection().flushAll();

		// 테스트 데이터 설정
		testLaundry = Laundry.builder()
			.laundryId(1L)
			.build();

		testMachine = Machine.builder()
			.machineId(1L)
			.laundry(testLaundry)
			.machineType(MachineType.WASHING)
			.usageStatus(UsageStatus.USABLE)
			.build();
	}

	@Test
	@DisplayName("애플리케이션 시작 시 모든 기계 데이터가 Redis에 저장된다")
	void initializeMachineData() {
		// given
		List<Machine> machines = Collections.singletonList(testMachine);
		when(machineRepository.findAll()).thenReturn(machines);

		// when
		machineRedisService.initializeMachineData();

		// then
		String machineKey = "machine:" + testMachine.getMachineId();
		String laundryMachinesKey = "laundry:machines:" + testLaundry.getLaundryId();

		MachineRedisDto savedMachine = (MachineRedisDto) redisTemplate.opsForValue().get(machineKey);
		Set<Object> laundryMachines = redisTemplate.opsForSet().members(laundryMachinesKey);

		assertThat(savedMachine).isNotNull();
		assertThat(savedMachine.getMachineId()).isEqualTo(testMachine.getMachineId());
		assertThat(laundryMachines).contains(testMachine.getMachineId().toString());
	}

	@Test
	@DisplayName("기계 정보를 Redis에 저장한다")
	void saveMachineToRedis() {
		// when
		machineRedisService.saveMachineToRedis(testMachine);

		// then
		String machineKey = "machine:" + testMachine.getMachineId();
		String laundryMachinesKey = "laundry:machines:" + testLaundry.getLaundryId();

		MachineRedisDto savedMachine = (MachineRedisDto) redisTemplate.opsForValue().get(machineKey);
		Set<Object> laundryMachines = redisTemplate.opsForSet().members(laundryMachinesKey);

		assertThat(savedMachine).isNotNull();
		assertThat(savedMachine.getMachineId()).isEqualTo(testMachine.getMachineId());
		assertThat(savedMachine.getMachineType()).isEqualTo(testMachine.getMachineType());
		assertThat(laundryMachines).contains(testMachine.getMachineId().toString());
	}

	@Test
	@DisplayName("기계 정보를 업데이트한다")
	void updateMachine() {
		// given
		MachineUpdateDto updateDto = MachineUpdateDto.builder()
			.machineId(1)
			.usageStatus(UsageStatus.UNUSABLE)
			.notes("고장")
			.build();

		machineRedisService.saveMachineToRedis(testMachine);
		testMachine.updateOf(updateDto);

		// when
		machineRedisService.updateMachine(testMachine);

		// then
		String machineKey = "machine:" + testMachine.getMachineId();
		MachineRedisDto updatedMachine = (MachineRedisDto) redisTemplate.opsForValue().get(machineKey);

		assertThat(updatedMachine).isNotNull();
		assertThat(updatedMachine.getUsageStatus()).isEqualTo(UsageStatus.UNUSABLE);
		assertThat(updatedMachine.getNotes()).isEqualTo("고장");
	}

	@Test
	@DisplayName("기계 정보를 삭제한다")
	void deleteMachine() {
		// given
		machineRedisService.saveMachineToRedis(testMachine);

		// when
		machineRedisService.deleteMachine(testMachine.getMachineId());

		// then
		String machineKey = "machine:" + testMachine.getMachineId();
		MachineRedisDto deletedMachine = (MachineRedisDto) redisTemplate.opsForValue().get(machineKey);

		assertThat(deletedMachine).isNull();
	}

	@Test
	@DisplayName("세탁소 ID로 기계 목록을 조회한다")
	void getMachinesByLaundryId() {
		// given
		Machine machine2 = Machine.builder()
			.machineId(2L)
			.laundry(testLaundry)
			.machineType(MachineType.DRYING)
			.usageStatus(UsageStatus.RESERVING)
			.build();

		machineRedisService.saveMachineToRedis(testMachine);
		machineRedisService.saveMachineToRedis(machine2);

		// when
		List<MachineRedisDto> machines = machineRedisService.getMachinesByLaundryId(testLaundry.getLaundryId());

		// then
		assertThat(machines).hasSize(2);
		assertThat(machines)
			.extracting(MachineRedisDto::getMachineId)
			.containsExactlyInAnyOrder(1L, 2L);
		assertThat(machines.get(0).getMachineId()).isEqualTo(testMachine.getMachineId());
		assertThat(machines.get(1).getMachineId()).isEqualTo(machine2.getMachineId());
		assertThat(machines.get(0).getUsageStatus()).isEqualTo(UsageStatus.USABLE);
		assertThat(machines.get(1).getUsageStatus()).isEqualTo(UsageStatus.RESERVING);
	}
	//
	@Test
	@DisplayName("존재하지 않는 세탁소 ID로 조회 시 빈 리스트를 반환한다")
	void getMachinesByLaundryId_NotFound() {
		// when
		List<MachineRedisDto> machines = machineRedisService.getMachinesByLaundryId(999L);

		// then
		assertThat(machines).isEmpty();
	}

	@Test
	@DisplayName("기계를 사용 상태로 변경한다")
	void useMachine() {
		// given
		machineRedisService.saveMachineToRedis(testMachine);
		long customerId = 100L;
		LocalDateTime courseTime = LocalDateTime.now().plusHours(1);

		// when
		machineRedisService.useMachine(customerId, testMachine, courseTime);

		// then
		String machineKey = "machine:" + testMachine.getMachineId();
		MachineRedisDto usedMachine = (MachineRedisDto) redisTemplate.opsForValue().get(machineKey);

		assertThat(usedMachine).isNotNull();
		assertThat(usedMachine.getCustomerId()).isEqualTo(customerId);
		assertThat(usedMachine.getEndTime().truncatedTo(ChronoUnit.SECONDS))
			.isEqualTo(LocalDateTime.now().plusMinutes(courseTime.getMinute()).truncatedTo(ChronoUnit.SECONDS));
	}

	@Test
	@DisplayName("기계를 예약 상태로 변경한다")
	void reserveMachine() {
		// given
		machineRedisService.saveMachineToRedis(testMachine);
		long customerId = 100L;

		// when
		machineRedisService.reserveMachine(customerId, testMachine);

		// then
		String machineKey = "machine:" + testMachine.getMachineId();
		MachineRedisDto reservedMachine = (MachineRedisDto) redisTemplate.opsForValue().get(machineKey);

		assertThat(reservedMachine).isNotNull();
		assertThat(reservedMachine.getCustomerId()).isEqualTo(customerId);
		assertThat(reservedMachine.getUsageStatus()).isEqualTo(UsageStatus.RESERVING);
		assertThat(reservedMachine.getEndTime().truncatedTo(ChronoUnit.SECONDS))
			.isEqualTo(LocalDateTime.now().plusMinutes(15).truncatedTo(ChronoUnit.SECONDS));
	}

	@Test
	@DisplayName("기계를 초기 상태로 리셋한다")
	void resetMachine() {
		// given
		machineRedisService.saveMachineToRedis(testMachine);
		machineRedisService.useMachine(100L, testMachine, LocalDateTime.now().plusHours(1));

		// when
		machineRedisService.resetMachine(testMachine);

		// then
		String machineKey = "machine:" + testMachine.getMachineId();
		MachineRedisDto resetMachine = (MachineRedisDto) redisTemplate.opsForValue().get(machineKey);

		assertThat(resetMachine).isNotNull();
		assertThat(resetMachine.getCustomerId()).isNull();
		assertThat(resetMachine.getEndTime()).isNull();
	}

	@Test
	@DisplayName("Redis에 데이터가 없을 때 새로 생성한다")
	void getMachineRedisDto_WhenDataMissing() {
		// given - Redis에 데이터가 없는 상태

		// when
		machineRedisService.updateMachine(testMachine);

		// then
		String machineKey = "machine:" + testMachine.getMachineId();
		String laundryMachinesKey = "laundry:machines:" + testLaundry.getLaundryId();

		MachineRedisDto machine = (MachineRedisDto) redisTemplate.opsForValue().get(machineKey);
		Set<Object> laundryMachines = redisTemplate.opsForSet().members(laundryMachinesKey);

		assertThat(machine).isNotNull();
		assertThat(machine.getMachineId()).isEqualTo(testMachine.getMachineId());
		assertThat(laundryMachines).isNotNull();
		assertThat(laundryMachines).contains(testMachine.getMachineId().toString());
	}
}