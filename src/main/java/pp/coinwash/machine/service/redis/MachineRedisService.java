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
import pp.coinwash.machine.domain.type.UsageStatus;

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
		redisTemplate.opsForSet().add(laundryMachinesKey, machine.getMachineId().toString());
	}

	public void updateMachine(Machine machine) {
		String machineKey = MACHINE_KEY_PREFIX + machine.getMachineId();

		MachineRedisDto machineRedis = getMachineRedisDto(machine);

		machineRedis.updateMachine(machine);

		redisTemplate.opsForValue().set(machineKey, machineRedis);
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
			MachineRedisDto machine = (MachineRedisDto)redisTemplate.opsForValue().get(machineKey);
			if (machine != null) {

				if (machine.getEndTime() != null
					&& machine.getEndTime().isBefore(LocalDateTime.now())
					&& machine.getUsageStatus() != UsageStatus.UNUSABLE) {

					machine.reset();
				}
				machines.add(machine);
			}
		}

		return machines;
	}

	public void useMachine(Machine machine) {
		String machineKey = MACHINE_KEY_PREFIX + machine.getMachineId();

		MachineRedisDto machineRedis = getMachineRedisDto(machine);

		machineRedis.useMachine(machine.getCustomerId(), machine.getEndTime());

		redisTemplate.opsForValue().set(machineKey, machineRedis);
	}

	public void reserveMachine(Machine machine) {
		String machineKey = MACHINE_KEY_PREFIX + machine.getMachineId();

		MachineRedisDto machineRedis = getMachineRedisDto(machine);

		machineRedis.reserveMachine(machine.getCustomerId());

		redisTemplate.opsForValue().set(machineKey, machineRedis);
	}

	public void resetMachine(Machine machine) {
		String machineKey = MACHINE_KEY_PREFIX + machine.getMachineId();

		MachineRedisDto machineRedis = getMachineRedisDto(machine);

		machineRedis.reset();

		redisTemplate.opsForValue().set(machineKey, machineRedis);
	}

	private MachineRedisDto getMachineRedisDto(Machine machine) {
		String machineKey = MACHINE_KEY_PREFIX + machine.getMachineId();
		String laundryMachinesKey = LAUNDRY_MACHINES_KEY_PREFIX + machine.getLaundry().getLaundryId();

		MachineRedisDto dto = (MachineRedisDto)redisTemplate.opsForValue().get(machineKey);

		if (dto == null) {
			log.warn("Redis에서 기계 데이터 누락 감지, 재생성: machineId={}", machine.getMachineId());

			// 세탁소별 기계 목록에도 추가 (누락 방지)
			redisTemplate.opsForSet().add(laundryMachinesKey, machine.getMachineId().toString());
			return MachineRedisDto.from(machine);
		} else {
			return dto;
		}
	}
}
