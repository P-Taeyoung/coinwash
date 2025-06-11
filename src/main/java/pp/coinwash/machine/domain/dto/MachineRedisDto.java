package pp.coinwash.machine.domain.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.domain.type.MachineType;
import pp.coinwash.machine.domain.type.UsageStatus;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MachineRedisDto {
	private Long machineId;
	private Long laundryId;
	private MachineType machineType;
	private UsageStatus usageStatus;
	private LocalDateTime endTime;
	private Long customerId;
	private String notes;

	public static MachineRedisDto from(Machine machine) {
		return MachineRedisDto.builder()
			.machineId(machine.getMachineId())
			.laundryId(machine.getLaundry().getLaundryId())
			.machineType(machine.getMachineType())
			.usageStatus(machine.getUsageStatus())
			.endTime(machine.getEndTime())
			.customerId(machine.getCustomerId())
			.notes(machine.getNotes())
			.build();
	}

	public void updateMachine(Machine machine) {
		this.usageStatus = machine.getUsageStatus();
		this.notes = machine.getNotes();
	}

	public void useMachine(long customerId, LocalDateTime courseTime) {
		this.customerId = customerId;
		this.usageStatus = UsageStatus.USING;
		this.endTime = courseTime;
	}

	public void reserveMachine(long customerId) {
		this.customerId = customerId;
		this.usageStatus = UsageStatus.RESERVING;
		this.endTime = LocalDateTime.now().plusMinutes(15);
	}

	public void reset() {
		this.customerId = null;
		this.usageStatus = UsageStatus.USABLE;
		this.endTime = null;
	}
}
