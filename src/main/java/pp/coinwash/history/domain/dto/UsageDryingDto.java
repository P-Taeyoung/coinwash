package pp.coinwash.history.domain.dto;

import lombok.Builder;
import pp.coinwash.history.domain.type.DryingCourse;

@Builder
public record UsageDryingDto(
	long machineId,
	DryingCourse course
) {
}
