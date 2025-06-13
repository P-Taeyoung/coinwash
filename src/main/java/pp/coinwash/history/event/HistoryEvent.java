package pp.coinwash.history.event;

import pp.coinwash.history.domain.dto.HistoryRequestDto;
import pp.coinwash.machine.domain.entity.Machine;

public record HistoryEvent(
	HistoryRequestDto requestDto,
	Machine machine
) {
}
