package pp.coinwash.history.domain.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import pp.coinwash.history.domain.type.HistoryType;

@Builder
public record HistoryRequestDto(
	long customerId,
	HistoryType historyType,
	LocalDateTime startTime,
	LocalDateTime endTime
) {
}
