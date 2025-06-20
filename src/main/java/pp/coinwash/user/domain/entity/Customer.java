package pp.coinwash.user.domain.entity;

import java.time.LocalDateTime;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
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

	@Column(columnDefinition = "POINT SRID 4326", nullable = false)
	private Point location;

	private LocalDateTime deletedAt;

	@Version
	private Long version;

	public static Customer of(CustomerSignUpDto dto, PasswordEncoder passwordEncoder) {
		return Customer.builder()
			.loginId(dto.id())
			.password(passwordEncoder.encode(dto.password()))
			.name(dto.name())
			.phone(dto.phone())
			.points(0)
			.address(dto.address())
			.location(createPoint(dto.longitude(), dto.latitude()))
			.build();
	}

	public void update(CustomerUpdateDto dto) {
		this.phone = dto.phone();
		this.address = dto.address();
		this.location = createPoint(dto.longitude(), dto.latitude());
	}

	public void delete() {
		this.deletedAt = LocalDateTime.now();
	}

	public void usePoints(int points) {
		this.points -= points;
	}

	public void earnPoints(int points) {
		this.points += points;
	}

	private static Point createPoint(double longitude, double latitude) {
		GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
		return geometryFactory.createPoint(new Coordinate(longitude, latitude));
	}

}
