package pp.coinwash.notification.domain.dto;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record NotificationRequestDto(
	Long customerId,
	String message,
	LocalDateTime endTime
) {
	public static NotificationRequestDto createNotificationRequestDto(Long customerId, String message, LocalDateTime endTime) {
		return NotificationRequestDto.builder()
			.customerId(customerId)
			.message(message)
			.endTime(endTime)
			.build();
	}
}
