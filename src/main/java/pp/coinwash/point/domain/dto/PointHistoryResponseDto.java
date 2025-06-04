package pp.coinwash.point.domain.dto;

import lombok.Builder;
import pp.coinwash.point.domain.entity.PointHistory;
import pp.coinwash.point.domain.type.PointType;

@Builder
public record PointHistoryResponseDto(
	PointType pointType,
	int changedPoint
) {
	public static PointHistoryResponseDto from(PointHistory pointHistory) {
		return PointHistoryResponseDto.builder()
			.pointType(pointHistory.getPointType())
			.changedPoint(pointHistory.getChangedPoints())
			.build();
	}
}
