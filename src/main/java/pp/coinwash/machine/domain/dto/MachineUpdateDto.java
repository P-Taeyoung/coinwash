package pp.coinwash.machine.domain.dto;

import lombok.Builder;

@Builder
public record MachineUpdateDto(
	String notes
) {
}
