package pp.coinwash.history.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import pp.coinwash.history.domain.type.HistoryType;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.history.domain.dto.HistoryRequestDto;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class History extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long historyId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "machine_id", nullable = false)
	private Machine machine;

	private long customerId;

	//예약 or 사용 내역
	@Enumerated(EnumType.STRING)
	private HistoryType historyType;
	//사용 시작 시간
	private LocalDateTime startTime;
	//사용 종료 시간
	private LocalDateTime endTime;

	public static History of(HistoryRequestDto requestDto, Machine machine) {
		return History.builder()
			.machine(machine)
			.customerId(requestDto.customerId())
			.historyType(requestDto.historyType())
			.startTime(requestDto.startTime())
			.endTime(requestDto.endTime())
			.build();
	}
}
