package pp.coinwash.usage.domain.dto;

import lombok.Builder;
import pp.coinwash.usage.domain.type.WashingCourse;

@Builder
public record UsageWashingDto(
	long machineId,
	WashingCourse course
) {
}
