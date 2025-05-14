package pp.coinwash.user.domain.entity;

import org.springframework.security.crypto.bcrypt.BCrypt;

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

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long customerId;

	private String id;
	private String password;
	private String name;
	private String phone;

	private int points;

	private String address;
	private Long latitude;
	private Long longitude;

	public static Customer of(CustomerSignUpDto dto) {
		return Customer.builder()
			.id(dto.id())
			.password(BCrypt.hashpw(dto.password(), BCrypt.gensalt()))
			.name(dto.name())
			.phone(dto.phone())
			.points(0)
			.address(dto.address())
			.latitude(dto.latitude())
			.longitude(dto.longitude())
			.build();
	}

}
