package pp.coinwash.machine.domain.dto;

import lombok.Builder;
import pp.coinwash.machine.domain.type.MachineType;

@Builder
public record MachineRegisterDto(
	MachineType machineType,
	String notes
) {

}
