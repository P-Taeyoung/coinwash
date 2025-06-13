package pp.coinwash.notification.domain.dto;

import lombok.Builder;

@Builder
public record NotificationRequestDto(
	Long customerId,
	String message
) {
	public static NotificationRequestDto createNotificationRequestDto(Long customerId, String message) {
		return NotificationRequestDto.builder()
			.customerId(customerId)
			.message(message)
			.build();
	}
}
