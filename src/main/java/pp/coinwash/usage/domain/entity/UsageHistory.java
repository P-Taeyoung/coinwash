package pp.coinwash.usage.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pp.coinwash.common.entity.BaseEntity;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.domain.type.MachineType;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsageHistory extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long historyId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "machine_id", nullable = false)
	private Machine machine;

	private long customerId;

	//사용 시작 시간
	private LocalDateTime startTime;
	//사용 종료 시간
	private LocalDateTime endTime;


}
