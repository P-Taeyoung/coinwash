package pp.coinwash.machine.domain.entity;

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
import pp.coinwash.history.domain.type.DryingCourse;
import pp.coinwash.history.domain.type.WashingCourse;
import pp.coinwash.laundry.domain.entity.Laundry;
import pp.coinwash.machine.domain.dto.MachineRegisterDto;
import pp.coinwash.machine.domain.dto.MachineUpdateDto;
import pp.coinwash.machine.domain.type.MachineType;
import pp.coinwash.machine.domain.type.UsageStatus;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Machine extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long machineId;

	@ManyToOne(fetch = FetchType.LAZY)
	//TODO 테스트 때문에 null 허용 추후 다시 변경
	@JoinColumn(name = "laundry_id" /*nullable = false*/)
	private Laundry laundry;

	@Enumerated(EnumType.STRING)
	private MachineType machineType;

	@Enumerated(EnumType.STRING)
	private UsageStatus usageStatus;

	//기계 사용&에약 종료 시간
	private LocalDateTime endTime;

	//예약자 정보
	private Long customerId;

	//비고
	private String notes;

	private LocalDateTime deletedAt;

	public static Machine of(MachineRegisterDto dto, Laundry laundry) {
		return Machine.builder()
			.laundry(laundry)
			.machineType(dto.machineType())
			.usageStatus(UsageStatus.USABLE)
			.endTime(null)
			.customerId(null)
			.notes(dto.notes())
			.build();
	}

	public void updateOf(MachineUpdateDto dto) {
		this.usageStatus = dto.usageStatus();
		this.notes = dto.notes();
	}

	public void delete() {
		this.deletedAt = LocalDateTime.now();
	}

	public void useWashing(long customerId, WashingCourse course) {
		this.customerId = customerId;
		this.usageStatus = UsageStatus.USING;
		this.endTime = LocalDateTime.now().plusMinutes(course.getCourseTime());
	}

	public void useDrying(long customerId, DryingCourse course) {
		this.customerId = customerId;
		this.usageStatus = UsageStatus.USING;
		this.endTime = LocalDateTime.now().plusMinutes(course.getCourseTime());
	}

	public void reserve(long customerId) {
		this.customerId = customerId;
		this.usageStatus = UsageStatus.RESERVING;
		this.endTime = LocalDateTime.now().plusMinutes(15);
	}

	public void reset() {
		this.usageStatus = UsageStatus.USABLE;
		this.endTime = null;
		this.customerId = null;
	}
}
