package pp.coinwash.machine.domain.dto;

import lombok.Builder;
import pp.coinwash.machine.domain.type.UsageStatus;

@Builder
public record MachineUpdateDto(
	long machineId,
	UsageStatus usageStatus,
	String notes
) {
}
