package pp.coinwash.machine.application;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.lettuce.core.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pp.coinwash.common.exception.CustomException;
import pp.coinwash.common.exception.ErrorCode;
import pp.coinwash.machine.domain.dto.UsingDryingDto;
import pp.coinwash.machine.domain.dto.UsingWashingDto;
import pp.coinwash.machine.domain.entity.Machine;
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
