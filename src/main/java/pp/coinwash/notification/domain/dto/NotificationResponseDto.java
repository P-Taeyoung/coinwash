package pp.coinwash.notification.domain.dto;

import lombok.Builder;
import pp.coinwash.notification.domain.entity.Notification;

@Builder
public record NotificationResponseDto(
	Long notificationId,
	Long customerId,
	String message,
	boolean checked
) {
	public static NotificationResponseDto from(Notification notification) {
		return NotificationResponseDto.builder()
			.notificationId(notification.getNotificationId())
			.customerId(notification.getCustomerId())
			.message(notification.getMessage())
			.checked(notification.isChecked())
			.build();
	}
}
