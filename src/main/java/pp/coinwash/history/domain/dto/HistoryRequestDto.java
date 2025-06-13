package pp.coinwash.history.domain.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import pp.coinwash.history.domain.type.DryingCourse;
import pp.coinwash.history.domain.type.HistoryType;
import pp.coinwash.history.domain.type.WashingCourse;

@Builder
public record HistoryRequestDto(
	long customerId,
	HistoryType historyType,
	LocalDateTime startTime,
	LocalDateTime endTime
) {
	public static HistoryRequestDto createWashingHistory(Long customerId,
		LocalDateTime startTime,
		WashingCourse course) {
		return HistoryRequestDto.builder()
			.customerId(customerId)
			.historyType(HistoryType.USAGE)
			.startTime(startTime)
			.endTime(startTime.plusMinutes(course.getCourseTime()))
			.build();
	}

	public static HistoryRequestDto createDryingHistory(Long customerId,
		LocalDateTime startTime,
		DryingCourse course) {
		return HistoryRequestDto.builder()
			.customerId(customerId)
			.historyType(HistoryType.USAGE)
			.startTime(startTime)
			.endTime(startTime.plusMinutes(course.getCourseTime()))
			.build();
	}

	public static HistoryRequestDto createReservationHistory(Long customerId,
		LocalDateTime startTime) {
		return HistoryRequestDto.builder()
			.customerId(customerId)
			.historyType(HistoryType.RESERVATION)
			.startTime(startTime)
			.endTime(startTime.plusMinutes(15))
			.build();
	}

	public static HistoryRequestDto createCancelReservationHistory(Long customerId) {
		return HistoryRequestDto.builder()
			.customerId(customerId)
			.historyType(HistoryType.CANCEL_RESERVATION)
			.startTime(null)
			.endTime(null)
			.build();
	}
}
