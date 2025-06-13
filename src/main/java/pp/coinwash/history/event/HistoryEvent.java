package pp.coinwash.history.event;

import lombok.Builder;
import pp.coinwash.history.domain.dto.HistoryRequestDto;
import pp.coinwash.machine.domain.entity.Machine;

@Builder
public record HistoryEvent(
	HistoryRequestDto requestDto,
	Machine machine
) {
	public static HistoryEvent of(HistoryRequestDto requestDto, Machine machine) {
		return HistoryEvent.builder()
			.requestDto(requestDto)
			.machine(machine)
			.build();
	}
}
