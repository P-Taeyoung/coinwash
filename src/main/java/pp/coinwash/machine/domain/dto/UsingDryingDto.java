package pp.coinwash.machine.domain.dto;

import lombok.Builder;
import pp.coinwash.history.domain.type.DryingCourse;

@Builder
public record UsingDryingDto(
	long machineId,
	DryingCourse course
) {
}
