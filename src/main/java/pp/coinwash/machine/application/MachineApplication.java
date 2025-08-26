package pp.coinwash.machine.application;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.lettuce.core.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pp.coinwash.common.exception.CustomException;
import pp.coinwash.common.exception.ErrorCode;
import pp.coinwash.machine.domain.dto.MachineResponseDto;
import pp.coinwash.machine.domain.dto.UsingDryingDto;
import pp.coinwash.machine.domain.dto.UsingWashingDto;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.service.MachineManageService;
import pp.coinwash.machine.service.ReservingMachineService;
import pp.coinwash.machine.service.UsingMachineService;
import pp.coinwash.machine.service.redis.MachineRedisService;

@Service
@RequiredArgsConstructor
@Slf4j
public class MachineApplication {

	private final MachineRedisService redisService;
	private final UsingMachineService usingService;
	private final ReservingMachineService reservingService;
	private final MachineManageService manageService;

	@Transactional(readOnly = true)
	public List<MachineResponseDto> getMachinesByLaundryId(long laundryId) {
		long apiStart = System.nanoTime();
		//먼저 레디스에서 조회
		try {
			// 응답속도 체크를 위한 시간 데이터
			long redisStart = System.nanoTime();

			List<MachineResponseDto> redisResult = redisService.getMachinesByLaundryIdAsync(laundryId)
				.stream()
				.map(MachineResponseDto::fromRedis)
				.toList();
			long redisEnd = System.nanoTime();
			log.info("[perf] phase=redisFetch laundryId={} size={} timeMs={}",
				laundryId,
				redisResult.size(),
				String.format("%.3f", (redisEnd - redisStart)/1_000_000.0));

			log.info("기기 조회 데이터 : {}", redisResult);

			// Redis에서 데이터가 있으면 반환
			if (!redisResult.isEmpty()) {
				log.info("[perf] phase=apiTotal laundryId={} source=REDIS timeMs={}",
					laundryId,
					String.format("%.3f", (System.nanoTime() - apiStart)/1_000_000.0));
				return redisResult;
			}

			// Redis에 데이터가 없으면 DB에서 조회
			log.info("세탁소 ID {}에 대한 Redis 데이터가 없어 DB에서 조회합니다", laundryId);
			long dbStart = System.nanoTime();
			List<MachineResponseDto> dbResult = manageService.getMachinesByLaundryId(laundryId);
			long dbEnd = System.nanoTime();
			log.info("[perf] phase=dbFetch laundryId={} size={} timeMs={}",
				laundryId,
				dbResult.size(),
				String.format("%.3f", (dbEnd - dbStart)/1_000_000.0));

			log.info("[perf] phase=apiTotal laundryId={} source=DB timeMs={}",
				laundryId,
				String.format("%.3f", (System.nanoTime() - apiStart)/1_000_000.0));

			return dbResult;

		} catch (RedisException e) {
			// Redis 관련 예외만 처리
			long fallbackStart = System.nanoTime();
			log.warn("세탁소 ID {}의 Redis 조회 중 오류 발생, DB로 대체 조회합니다. 오류: {}",
				laundryId, e.getMessage());
			List<MachineResponseDto> dbResult = manageService.getMachinesByLaundryId(laundryId);
			long fallbackEnd = System.nanoTime();
			log.info("[perf] phase=redisError fallback=dbFetch laundryId={} size={} dbTimeMs={}",
				laundryId,
				dbResult.size(),
				String.format("%.3f", (fallbackEnd - fallbackStart)/1_000_000.0));
			log.info("[perf] phase=apiTotal laundryId={} source=DB_FALLBACK timeMs={}",
				laundryId,
				String.format("%.3f", (System.nanoTime() - apiStart)/1_000_000.0));
			return dbResult;


		} catch (Exception e) {
			// 예상치 못한 예외는 다시 던지기
			log.error("세탁소 ID {}의 기계 정보 조회 중 예상치 못한 오류가 발생했습니다",
				laundryId, e);
			throw e;
		}
	}

	@Transactional
	public void useWashing(long customerId, UsingWashingDto usingWashingDto) {
		executeMachineOperation(
			usingWashingDto.machineId(),
			() -> usingService.useWashing(customerId, usingWashingDto),
			redisService::useMachine,
			"세탁기 사용에 실패했습니다"
		);
	}

	@Transactional
	public void useDrying(long customerId, UsingDryingDto usingDryingDto) {
		executeMachineOperation(
			usingDryingDto.machineId(),
			() -> usingService.useDrying(customerId, usingDryingDto),
			redisService::useMachine,
			"건조기 사용에 실패했습니다"
		);
	}

	@Transactional
	public void reserveMachine(long machineId, long customerId) {
		executeMachineOperation(
			machineId,
			() -> reservingService.reserveMachine(machineId, customerId),
			redisService::reserveMachine,
			"기계 예약에 실패했습니다"
		);
	}

	@Transactional
	public void cancelReservingMachine(long machineId, long customerId) {
		executeMachineOperation(
			machineId,
			() -> reservingService.cancelReserveMachine(machineId, customerId),
			redisService::resetMachine,
			"기계 예약 취소에 실패했습니다"
		);
	}

	private void executeMachineOperation(
		long machineId,
		Supplier<Machine> dbOperation,
		Consumer<Machine> redisOperation,
		String errorMessage) {
		try {
			Machine machine = dbOperation.get();
			redisOperation.accept(machine);

		} catch (RedisException e) {
			log.error("기계 {} Redis 데이터 업데이트 실패로 전체 롤백", machineId, e);
			throw new CustomException(ErrorCode.FAILED_TO_CHANGE_MACHINE_STATUS);
		}
	}

	//성능 테스트용: Redis에서만 조회
	@Transactional(readOnly = true)
	public List<MachineResponseDto> getMachinesFromRedisOnly(long laundryId) {
		long start = System.nanoTime();

		try {
			List<MachineResponseDto> result = redisService.getMachinesByLaundryIdAsync(laundryId)
				.stream()
				.map(MachineResponseDto::fromRedis)
				.toList();

			long end = System.nanoTime();
			log.debug("[perf-test] Redis전용조회 laundryId={} size={} timeMs={}",
				laundryId, result.size(), String.format("%.3f", (end - start)/1_000_000.0));

			return result;

		} catch (Exception e) {
			log.warn("Redis 전용 조회 실패 - laundryId: {}, error: {}", laundryId, e.getMessage());
			return Collections.emptyList();
		}
	}

	// 성능 테스트용: MySQL에서만 조회
	@Transactional(readOnly = true)
	public List<MachineResponseDto> getMachinesFromMySQLOnly(long laundryId) {
		long start = System.nanoTime();

		try {
			List<MachineResponseDto> result = manageService.getMachinesByLaundryId(laundryId);

			long end = System.nanoTime();
			log.debug("[perf-test] MySQL전용조회 laundryId={} size={} timeMs={}",
				laundryId, result.size(), String.format("%.3f", (end - start)/1_000_000.0));

			return result;

		} catch (Exception e) {
			log.error("MySQL 전용 조회 실패 - laundryId: {}", laundryId, e);
			throw e;
		}
	}

	// 성능 테스트용: Redis 캐시 통계 정보
	public Map<String, Object> getRedisStats(List<Long> laundryIds) {
		Map<String, Object> stats = new HashMap<>();
		int totalLaundries = laundryIds.size();
		int cachedLaundries = 0;
		int totalMachines = 0;

		for (Long laundryId : laundryIds) {
			try {
				List<MachineResponseDto> machines = getMachinesFromRedisOnly(laundryId);
				if (!machines.isEmpty()) {
					cachedLaundries++;
					totalMachines += machines.size();
				}
			} catch (Exception e) {
				log.debug("세탁소 {} 통계 수집 실패", laundryId);
			}
		}

		stats.put("totalLaundries", totalLaundries);
		stats.put("cachedLaundries", cachedLaundries);
		stats.put("cacheHitRate", (double) cachedLaundries / totalLaundries * 100);
		stats.put("totalCachedMachines", totalMachines);

		return stats;
	}
}
