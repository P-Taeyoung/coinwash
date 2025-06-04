package pp.coinwash.point.domain.dto;

import lombok.Builder;
import pp.coinwash.point.domain.type.PointType;

@Builder
public record PointHistoryRequestDto(
	PointType pointType,
	long customerId,
	int changedPoint
) {
}
