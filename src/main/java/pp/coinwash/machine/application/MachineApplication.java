package pp.coinwash.machine.application;

import java.util.List;
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
		//먼저 레디스에서 조회
		try {
			List<MachineResponseDto> redisResult = redisService.getMachinesByLaundryId(laundryId)
				.stream()
				.map(MachineResponseDto::fromRedis)
				.toList();

			log.info("기기 조회 데이터 : {}", redisResult);

			// Redis에서 데이터가 있으면 반환
			if (!redisResult.isEmpty()) {
				return redisResult;
			}

			// Redis에 데이터가 없으면 DB에서 조회
			log.info("세탁소 ID {}에 대한 Redis 데이터가 없어 DB에서 조회합니다", laundryId);
			return manageService.getMachinesByLaundryId(laundryId);

		} catch (RedisException e) {
			// Redis 관련 예외만 처리
			log.warn("세탁소 ID {}의 Redis 조회 중 오류 발생, DB로 대체 조회합니다. 오류: {}",
				laundryId, e.getMessage());
			return manageService.getMachinesByLaundryId(laundryId);

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
}
