package pp.coinwash.history.domain.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import pp.coinwash.history.domain.entity.UsageHistory;

@Builder
public record UsageHistoryResponseDto(
	long historyId,
	long machineId,
	String laundryAddress,
	LocalDateTime startTime,
	LocalDateTime endTime
) {

	public static UsageHistoryResponseDto from(UsageHistory history) {

		return UsageHistoryResponseDto.builder()
			.historyId(history.getHistoryId())
			.machineId(history.getMachine().getMachineId())
			.laundryAddress(history.getMachine().getLaundry().getAddressName())
			.startTime(history.getStartTime())
			.endTime(history.getEndTime())
			.build();
	}
}
