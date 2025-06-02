package pp.coinwash.point.domain.dto;

import lombok.Builder;

@Builder
public record PointHistoryRequestDto(
	long customerId,
	int changedPoint
) {
}
