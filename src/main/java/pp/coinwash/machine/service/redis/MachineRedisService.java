package pp.coinwash.machine.service.redis;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
	// ⭐ Hash 구조로 변경: laundry:machines:{laundryId} -> Hash(machineId -> MachineRedisDto)
	private static final String LAUNDRY_MACHINES_HASH_PREFIX = "laundry:machines:";

	@EventListener(ApplicationReadyEvent.class)
	@Transactional(readOnly = true)
	public void initializeMachineData() {
		try {
			log.info("Redis 기계 데이터 초기화 시작...");

			List<Machine> machines = machineRepository.findAll();

			//세탁소별로 그룹화해서 Hash로 한 번에 저장
			Map<Long, List<Machine>> machinesByLaundry = machines.stream()
				.collect(Collectors.groupingBy(m -> m.getLaundry().getLaundryId()));

			for (Map.Entry<Long, List<Machine>> entry : machinesByLaundry.entrySet()) {
				saveMachinesHashToRedis(entry.getKey(), entry.getValue());
			}

			log.info("저장된 기계 수: {}, 세탁소 수: {}", machines.size(), machinesByLaundry.size());
		} catch (Exception e) {
			log.error("Redis 초기화 실패: {}", e.getMessage(), e);
		}
	}

	// ⭐ 세탁소별 기계들을 Hash로 한 번에 저장
	private void saveMachinesHashToRedis(Long laundryId, List<Machine> machines) {
		String hashKey = LAUNDRY_MACHINES_HASH_PREFIX + laundryId;

		Map<String, Object> machineMap = machines.stream()
			.collect(Collectors.toMap(
				m -> m.getMachineId().toString(),
				MachineRedisDto::from
			));

		if (!machineMap.isEmpty()) {
			redisTemplate.opsForHash().putAll(hashKey, machineMap);
		}

		// 개별 키도 유지 (기존 로직 호환성을 위해)
		for (Machine machine : machines) {
			String machineKey = MACHINE_KEY_PREFIX + machine.getMachineId();
			redisTemplate.opsForValue().set(machineKey, MachineRedisDto.from(machine));
		}
	}

	public void saveMachineToRedis(Machine machine) {
		MachineRedisDto dto = MachineRedisDto.from(machine);

		// 개별 키 저장 (기존 로직)
		String machineKey = MACHINE_KEY_PREFIX + machine.getMachineId();
		redisTemplate.opsForValue().set(machineKey, dto);

		// Hash에도 저장
		String hashKey = LAUNDRY_MACHINES_HASH_PREFIX + machine.getLaundry().getLaundryId();
		redisTemplate.opsForHash().put(hashKey, machine.getMachineId().toString(), dto);
	}

	// 핵심 최적화: Hash 기반 조회
	public List<MachineRedisDto> getMachinesByLaundryId(Long laundryId) {
		long totalStart = System.nanoTime();

		String hashKey = LAUNDRY_MACHINES_HASH_PREFIX + laundryId;

		// 1) Hash의 모든 값을 한 번에 조회 (Set members + MultiGet → Hash values)
		long hStart = System.nanoTime();
		List<Object> values = redisTemplate.opsForHash().values(hashKey);
		long hEnd = System.nanoTime();

		log.info("[perf] phase=hashValues laundryId={} count={} timeMs={}",
			laundryId, values.size(), ms(hStart, hEnd));

		if (values.isEmpty()) {
			log.info("[perf] phase=total laundryId={} status=EMPTY_HASH timeMs={}",
				laundryId, ms(totalStart, System.nanoTime()));
			return List.of();
		}

		// 2) 만료 검사 및 결과 생성
		long eStart = System.nanoTime();
		LocalDateTime now = LocalDateTime.now();
		List<MachineRedisDto> result = new ArrayList<>();
		Map<String, Object> toUpdate = new HashMap<>();

		for (Object value : values) {
			if (!(value instanceof MachineRedisDto dto)) continue;

			if (needsReset(dto, now)) {
				dto.reset();
				toUpdate.put(dto.getMachineId().toString(), dto);
			}
			result.add(dto);
		}
		long eEnd = System.nanoTime();

		log.info("[perf] phase=expireCheck laundryId={} total={} mutated={} timeMs={}",
			laundryId, values.size(), toUpdate.size(), ms(eStart, eEnd));

		// 3) 변경된 것만 Hash에 업데이트
		if (!toUpdate.isEmpty()) {
			long uStart = System.nanoTime();
			redisTemplate.opsForHash().putAll(hashKey, toUpdate);
			long uEnd = System.nanoTime();

			log.info("[perf] phase=hashUpdate laundryId={} updated={} timeMs={}",
				laundryId, toUpdate.size(), ms(uStart, uEnd));
		}

		long totalEnd = System.nanoTime();
		log.info("[perf] phase=total laundryId={} returnSize={} timeMs={}",
			laundryId, result.size(), ms(totalStart, totalEnd));

		return result;
	}

	// 비동기 업데이트 버전 (더 빠른 조회)
	public List<MachineRedisDto> getMachinesByLaundryIdAsync(Long laundryId) {
		String hashKey = LAUNDRY_MACHINES_HASH_PREFIX + laundryId;
		List<Object> values = redisTemplate.opsForHash().values(hashKey);

		if (values.isEmpty()) {
			return List.of();
		}

		LocalDateTime now = LocalDateTime.now();
		List<MachineRedisDto> result = new ArrayList<>();
		Map<String, Object> toUpdate = new HashMap<>();

		for (Object value : values) {
			if (!(value instanceof MachineRedisDto dto)) continue;

			if (needsReset(dto, now)) {
				dto.reset();
				toUpdate.put(dto.getMachineId().toString(), dto);
			}
			result.add(dto);
		}

		// ⭐ 비동기로 업데이트 (조회 성능에 영향 없음)
		if (!toUpdate.isEmpty()) {
			CompletableFuture.runAsync(() -> {
				try {
					redisTemplate.opsForHash().putAll(hashKey, toUpdate);
					log.debug("비동기 Hash 업데이트 완료: laundryId={}, count={}", laundryId, toUpdate.size());
				} catch (Exception e) {
					log.warn("비동기 Hash 업데이트 실패: laundryId={}, error={}", laundryId, e.getMessage());
				}
			});
		}

		return result;
	}

	public void updateMachine(Machine machine) {
		MachineRedisDto dto = MachineRedisDto.from(machine);

		// 개별 키 업데이트
		String machineKey = MACHINE_KEY_PREFIX + machine.getMachineId();
		redisTemplate.opsForValue().set(machineKey, dto);

		// Hash도 업데이트
		String hashKey = LAUNDRY_MACHINES_HASH_PREFIX + machine.getLaundry().getLaundryId();
		redisTemplate.opsForHash().put(hashKey, machine.getMachineId().toString(), dto);
	}

	public void deleteMachine(long machineId) {
		String machineKey = MACHINE_KEY_PREFIX + machineId;

		// 삭제 전에 laundryId 조회
		MachineRedisDto dto = (MachineRedisDto) redisTemplate.opsForValue().get(machineKey);

		if (dto != null && dto.getLaundryId() != null) {
			// Hash에서 삭제
			String hashKey = LAUNDRY_MACHINES_HASH_PREFIX + dto.getLaundryId();
			redisTemplate.opsForHash().delete(hashKey, String.valueOf(machineId));
			log.debug("Hash에서 기계 삭제: laundryId={}, machineId={}", dto.getLaundryId(), machineId);
		}

		// 개별 키 삭제
		redisTemplate.delete(machineKey);
		log.debug("개별 키 삭제: machineId={}", machineId);
	}

	public void useMachine(Machine machine) {
		updateMachineState(machine, (dto) ->
			dto.useMachine(machine.getCustomerId(), machine.getEndTime()));
	}

	public void reserveMachine(Machine machine) {
		updateMachineState(machine, (dto) ->
			dto.reserveMachine(machine.getCustomerId()));
	}

	public void resetMachine(Machine machine) {
		updateMachineState(machine, MachineRedisDto::reset);
	}

	// 상태 업데이트 로직 통합
	private void updateMachineState(Machine machine, Consumer<MachineRedisDto> updater) {
		MachineRedisDto dto = getMachineRedisDto(machine);
		updater.accept(dto);

		// 개별 키와 Hash 모두 업데이트
		String machineKey = MACHINE_KEY_PREFIX + machine.getMachineId();
		String hashKey = LAUNDRY_MACHINES_HASH_PREFIX + machine.getLaundry().getLaundryId();

		redisTemplate.opsForValue().set(machineKey, dto);
		redisTemplate.opsForHash().put(hashKey, machine.getMachineId().toString(), dto);
	}

	private MachineRedisDto getMachineRedisDto(Machine machine) {
		String machineKey = MACHINE_KEY_PREFIX + machine.getMachineId();
		MachineRedisDto dto = (MachineRedisDto) redisTemplate.opsForValue().get(machineKey);

		if (dto == null) {
			log.warn("Redis에서 기계 데이터 누락 감지, 재생성: machineId={}", machine.getMachineId());
			return MachineRedisDto.from(machine);
		}
		return dto;
	}

	private boolean needsReset(MachineRedisDto dto, LocalDateTime now) {
		return dto.getEndTime() != null
			&& dto.getEndTime().isBefore(now)
			&& dto.getUsageStatus() != UsageStatus.UNUSABLE;
	}

	private String ms(long start, long end) {
		return String.format("%.3f", (end - start) / 1_000_000.0);
	}
}
