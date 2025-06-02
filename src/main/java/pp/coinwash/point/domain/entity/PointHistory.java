package pp.coinwash.point.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pp.coinwash.common.entity.BaseEntity;
import pp.coinwash.point.domain.type.PointType;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PointHistory extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long PointHistoryId;

	private long customerId;

	private int changedPoints;

	private PointType pointType;

}
