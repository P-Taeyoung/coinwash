package pp.coinwash.laundry.domain.entity;


import java.time.LocalDateTime;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pp.coinwash.common.entity.BaseEntity;
import pp.coinwash.laundry.domain.dto.LaundryRegisterDto;
import pp.coinwash.laundry.domain.dto.LaundryUpdateDto;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "laundry", indexes = {
	@Index(name = "idx_laundry_location", columnList = "location")
})
public class Laundry extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long laundryId;

	private long ownerId;

	//주소 이름
	private String addressName;
	//위도, 경도 정보
	@Column(columnDefinition = "POINT SRID 4326", nullable = false)
	private Point location;
	//설명
	private String description;
	//운영현황
	private boolean opened;

	private LocalDateTime deletedAt;

	public static Laundry of(LaundryRegisterDto dto, long ownerId) {
		return Laundry.builder()
			.ownerId(ownerId)
			.addressName(dto.addressName())
			.location(createPoint(dto.longitude(), dto.latitude()))
			.description(dto.description())
			.opened(false)
			.build();
	}

	public void update(LaundryUpdateDto dto) {
		this.description = dto.description();
	}

	public void delete() {
		this.deletedAt = LocalDateTime.now();
	}

	public void changeLaundryStatus() {
		this.opened = !this.opened;
	}

	private static Point createPoint(double longitude, double latitude) {
		GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
		return geometryFactory.createPoint(new Coordinate(longitude, latitude));
	}
}
