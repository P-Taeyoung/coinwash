package pp.coinwash.usage.domain.dto;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record UsageHistoryResponseDto(
	long historyId,
	long machineId,
	String laundryAddress,
	LocalDateTime startTime,
	LocalDateTime endTime
) {
}
