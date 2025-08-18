package pp.coinwash.machine.application;

import static pp.coinwash.common.exception.ErrorCode.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.lettuce.core.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pp.coinwash.common.exception.CustomException;
import pp.coinwash.common.exception.ErrorCode;
import pp.coinwash.machine.domain.dto.MachineRegisterDto;
import pp.coinwash.machine.domain.dto.MachineResponseDto;
import pp.coinwash.machine.domain.dto.MachineUpdateDto;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.service.MachineManageService;
import pp.coinwash.machine.service.redis.MachineRedisService;

@Service
@RequiredArgsConstructor
@Slf4j
public class MachineManageApplication {

	private final MachineRedisService redisService;
	private final MachineManageService manageService;



	@Transactional
	public void registerMachines(List<MachineRegisterDto> dtos, long laundryId,long ownerId) {
		try {
			//DB에 먼저 저장
			List<Machine> machines = manageService.registerMachines(dtos, laundryId, ownerId);

			//레디스에 저장
			machines.forEach(redisService::saveMachineToRedis);

		} catch (RedisException e) {

			log.error("세탁소 기계 데이터 목록 {} Redis 저장 실패로 전체 롤백", laundryId, e);
			throw new CustomException(FAILED_TO_SAVE_MACHINES);
		}
	}

	@Transactional
	public void updateMachine(MachineUpdateDto updateDto, long ownerId) {
		try {
			log.info("기계 수정 요청 데이터 : {}", updateDto);
			Machine machine = manageService.updateMachine(updateDto, ownerId);

			redisService.updateMachine(machine);

		} catch (RedisException e) {
			log.error("기계 {} Redis 업데이트 실패로 전체 롤백", updateDto.machineId(), e);
			throw new CustomException(FAILED_TO_UPDATE_MACHINES);
		}
	}

	@Transactional
	public void deleteMachine(long machineId, long ownerId) {
		try {
			Machine machine = manageService.deleteMachine(machineId, ownerId);

			redisService.deleteMachine(machine);

		} catch (RedisException e) {
			log.error("기계 {} Redis 데이터 삭제 실패로 전체 롤백", machineId, e);
			throw new CustomException(FAILED_TO_DELETE_MACHINES);
		}
	}
}
