package pp.coinwash.notification.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pp.coinwash.common.entity.BaseEntity;
import pp.coinwash.notification.domain.dto.NotificationRequestDto;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Notification extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long NotificationId;

	private Long customerId;

	private String message;

	private boolean checked;

	public static Notification of(NotificationRequestDto dto) {
		return Notification.builder()
			.customerId(dto.customerId())
			.message(dto.message())
			.checked(false)
			.build();
	}

}
