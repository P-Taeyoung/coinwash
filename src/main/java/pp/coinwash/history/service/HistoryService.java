package pp.coinwash.history.service;

import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import pp.coinwash.common.dto.PagedResponseDto;
import pp.coinwash.history.event.HistoryEvent;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.history.domain.dto.HistoryRequestDto;
import pp.coinwash.history.domain.dto.HistoryResponseDto;
import pp.coinwash.history.domain.entity.History;
import pp.coinwash.history.domain.repository.HistoryRepository;

@Service
@RequiredArgsConstructor
public class HistoryService {

	private final HistoryRepository historyRepository;

	@Async("historyTaskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleHistoryEvent(HistoryEvent event) {

		createUsageHistory(event.requestDto(), event.machine());
	}

	public void createUsageHistory(HistoryRequestDto requestDto
		, Machine machine) {

		historyRepository.save(History.of(requestDto, machine));
	}

	public PagedResponseDto<HistoryResponseDto> getUsageHistoriesByCustomerId(
		long customerId, Pageable pageable) {

		return PagedResponseDto.from(historyRepository
			.findAllByCustomerId(customerId, pageable)
			.map(HistoryResponseDto::from));
	}
}
