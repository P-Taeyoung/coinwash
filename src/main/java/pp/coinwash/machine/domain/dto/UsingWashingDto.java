package pp.coinwash.machine.domain.dto;

import lombok.Builder;
import pp.coinwash.history.domain.type.WashingCourse;

@Builder
public record UsingWashingDto(
	Long machineId,
	WashingCourse course
) {
}
