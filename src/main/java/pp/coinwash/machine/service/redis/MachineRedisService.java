package pp.coinwash.machine.service.redis;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pp.coinwash.machine.domain.dto.MachineRedisDto;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.domain.repository.MachineRepository;

@Component
@Slf4j
@RequiredArgsConstructor
public class MachineRedisService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final MachineRepository machineRepository;

	private static final String MACHINE_KEY_PREFIX = "machine:";
	private static final String LAUNDRY_MACHINES_KEY_PREFIX = "laundry:machines:";

	// 어플리케이션 실행 시 모든 기계 레디스에 저장
	@PostConstruct
	public void initializeMachineData() {
		List<Machine> machines = machineRepository.findAll();
		
		for (Machine machine : machines) {
			saveMachineToRedis(machine);
		}
		
		log.info("저장된 기계 수 : {}", machines.size());
	}

	public void saveMachineToRedis(Machine machine) {
		MachineRedisDto dto = MachineRedisDto.from(machine);

		// 기계 개별 정보 저장
		String machineKey = MACHINE_KEY_PREFIX + machine.getMachineId();
		redisTemplate.opsForValue().set(machineKey, dto);

		// 세탁소별 기계 목록에 추가
		String laundryMachinesKey = LAUNDRY_MACHINES_KEY_PREFIX + machine.getLaundry().getLaundryId();
		redisTemplate.opsForSet().add(laundryMachinesKey, machine.getMachineId());
	}

	public void updateMachine(Machine machine) {
		String machineKey = MACHINE_KEY_PREFIX + machine.getMachineId();

		MachineRedisDto dto = getMachineRedisDto(machine.getMachineId());

		if (dto != null) {
			dto.updateMachine(machine);

			redisTemplate.opsForValue().set(machineKey, dto);
		}
	}

	public void deleteMachine(long machineId) {
		String machineKey = MACHINE_KEY_PREFIX + machineId;

		redisTemplate.delete(machineKey);
	}

	// 세탁소 ID로 기계 목록 조회
	public List<MachineRedisDto> getMachinesByLaundryId(Long laundryId) {
		String laundryMachinesKey = LAUNDRY_MACHINES_KEY_PREFIX + laundryId;
		Set<Object> machineIds = redisTemplate.opsForSet().members(laundryMachinesKey);

		if (machineIds == null || machineIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<MachineRedisDto> machines = new ArrayList<>();

		for (Object machineId : machineIds) {
			String machineKey = MACHINE_KEY_PREFIX + machineId;
			MachineRedisDto machine = (MachineRedisDto) redisTemplate.opsForValue().get(machineKey);
			if (machine != null) {
				machines.add(machine);
			}
		}

		return machines;
	}

	public void useMachine(long customerId, long machineId, LocalDateTime courseTime) {
		String machineKey = MACHINE_KEY_PREFIX + machineId;

		MachineRedisDto machine = getMachineRedisDto(machineId);

		if (machine != null) {
			machine.useMachine(customerId, courseTime);

			redisTemplate.opsForValue().set(machineKey, machine);
		}
	}

	public void reserveMachine(long customerId, long machineId) {
		String machineKey = MACHINE_KEY_PREFIX + machineId;

		MachineRedisDto machine = getMachineRedisDto(machineId);

		if (machine != null) {
			machine.reserveMachine(customerId);

			redisTemplate.opsForValue().set(machineKey, machine);
		}
	}

	public void resetMachine(long machineId) {
		String machineKey = MACHINE_KEY_PREFIX + machineId;

		MachineRedisDto machine = getMachineRedisDto(machineId);

		if(machine != null) {
			machine.reset();

			redisTemplate.opsForValue().set(machineKey, machine);
		}
	}

	private MachineRedisDto getMachineRedisDto(long machineId) {
		String machineKey = MACHINE_KEY_PREFIX + machineId;

		return (MachineRedisDto) redisTemplate.opsForValue().get(machineKey);
	}
}
