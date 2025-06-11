package pp.coinwash.machine.domain.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.domain.type.MachineType;
import pp.coinwash.machine.domain.type.UsageStatus;

@Builder
public record MachineResponseDto(
	long machineId,
	MachineType machineType,
	UsageStatus usageStatus,
	LocalDateTime endTime,
	String notes
) {
	public static MachineResponseDto from(Machine machine) {
		return MachineResponseDto.builder()
			.machineId(machine.getMachineId())
			.machineType(machine.getMachineType())
			.usageStatus(machine.getUsageStatus())
			.endTime(machine.getEndTime())
			.notes(machine.getNotes())
			.build();
	}

	public static MachineResponseDto fromRedis(MachineRedisDto dto) {
		return MachineResponseDto.builder()
			.machineId(dto.getMachineId())
			.machineType(dto.getMachineType())
			.usageStatus(dto.getUsageStatus())
			.endTime(dto.getEndTime())
			.notes(dto.getNotes())
			.build();
	}
}
