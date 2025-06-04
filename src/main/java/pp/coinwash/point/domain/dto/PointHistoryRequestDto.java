package pp.coinwash.point.domain.dto;

import lombok.Builder;
import pp.coinwash.point.domain.type.PointType;

@Builder
public record PointHistoryRequestDto(
	PointType pointType,
	long customerId,
	int changedPoint
) {
	public static PointHistoryRequestDto earnPoint(long customerId, int points) {
		return PointHistoryRequestDto.builder()
			.customerId(customerId)
			.changedPoint(points)
			.pointType(PointType.EARNED)
			.build();
	}
}
