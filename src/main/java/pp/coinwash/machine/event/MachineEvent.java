package pp.coinwash.machine.event;

import java.time.LocalDateTime;

import lombok.Builder;
import pp.coinwash.machine.domain.entity.Machine;

@Builder
public record MachineEvent(
	Machine machine,
	LocalDateTime endTime,
	ScheduleType scheduleType
) {
	public enum ScheduleType {
		USING, RESERVING, CANCEL_RESERVING
	}

	public static MachineEvent usingMachineEvent(Machine machine) {
		return MachineEvent.builder()
			.machine(machine)
			.endTime(machine.getEndTime())
			.scheduleType(ScheduleType.USING)
			.build();
	}

	public static MachineEvent reservingMachineEvent(Machine machine) {
		return MachineEvent.builder()
			.machine(machine)
			.endTime(machine.getEndTime())
			.scheduleType(ScheduleType.RESERVING)
			.build();
	}

	public static MachineEvent cancelReservingMachineEvent(Machine machine) {

		return MachineEvent.builder()
			.machine(machine)
			.scheduleType(ScheduleType.CANCEL_RESERVING)
			.build();

	}
}
