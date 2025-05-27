package pp.coinwash.usage.domain.dto;

import lombok.Builder;
import pp.coinwash.usage.domain.type.DryingCourse;

@Builder
public record UsageDryingDto(
	long machineId,
	DryingCourse course
) {
}
