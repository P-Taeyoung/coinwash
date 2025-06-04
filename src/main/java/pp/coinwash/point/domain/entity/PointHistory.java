package pp.coinwash.point.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pp.coinwash.common.entity.BaseEntity;
import pp.coinwash.point.domain.dto.PointHistoryRequestDto;
import pp.coinwash.point.domain.type.PointType;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PointHistory extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long pointHistoryId;

	private long customerId;

	private int changedPoints;

	@Enumerated(EnumType.STRING)
	private PointType pointType;

	public static PointHistory of(PointHistoryRequestDto dto) {
		return PointHistory.builder()
			.customerId(dto.customerId())
			.changedPoints(dto.changedPoint())
			.pointType(dto.pointType())
			.build();
	}

}
