package pp.coinwash.history.domain.dto;

import lombok.Builder;
import pp.coinwash.history.domain.type.WashingCourse;

@Builder
public record UsageWashingDto(
	long machineId,
	WashingCourse course
) {
}
