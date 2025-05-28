package pp.coinwash.history.domain.dto;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record UsageHistoryRequestDto(
	long customerId,
	LocalDateTime startTime,
	LocalDateTime endTime
) {
}
