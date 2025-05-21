package pp.coinwash.user.domain.entity;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pp.coinwash.common.entity.BaseEntity;
import pp.coinwash.user.domain.dto.CustomerSignUpDto;
import pp.coinwash.user.domain.dto.CustomerUpdateDto;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long customerId;

	private String loginId;
	private String password;
	private String name;
	private String phone;

	private int points;

	private String address;
	private Double latitude;
	private Double longitude;

	private LocalDateTime deletedAt;

	public static Customer of(CustomerSignUpDto dto, PasswordEncoder passwordEncoder) {
		return Customer.builder()
			.loginId(dto.id())
			.password(passwordEncoder.encode(dto.password()))
			.name(dto.name())
			.phone(dto.phone())
			.points(0)
			.address(dto.address())
			.latitude(dto.latitude())
			.longitude(dto.longitude())
			.build();
	}

	public void update(CustomerUpdateDto dto) {
		this.phone = dto.phone();
		this.address = dto.address();
		this.latitude = dto.latitude();
		this.longitude = dto.longitude();
	}

	public void delete() {
		this.deletedAt = LocalDateTime.now();
	}

}
