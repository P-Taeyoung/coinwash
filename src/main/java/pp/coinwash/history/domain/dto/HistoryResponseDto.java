package pp.coinwash.history.domain.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import pp.coinwash.history.domain.entity.History;
import pp.coinwash.history.domain.type.HistoryType;

@Builder
public record HistoryResponseDto(
	long historyId,
	long machineId,
	HistoryType historyType,
	String laundryAddress,
	LocalDateTime startTime,
	LocalDateTime endTime,
	LocalDateTime createdAt
) {

	public static HistoryResponseDto from(History history) {

		return HistoryResponseDto.builder()
			.historyId(history.getHistoryId())
			.machineId(history.getMachine().getMachineId())
			.laundryAddress(history.getMachine().getLaundry().getAddressName())
			.historyType(history.getHistoryType())
			.startTime(history.getStartTime())
			.endTime(history.getEndTime())
			.createdAt(history.getCreatedAt())
			.build();
	}
}
