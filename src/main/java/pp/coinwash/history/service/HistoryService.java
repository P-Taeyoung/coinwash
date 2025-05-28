package pp.coinwash.history.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pp.coinwash.common.dto.PagedResponseDto;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.history.domain.dto.HistoryRequestDto;
import pp.coinwash.history.domain.dto.HistoryResponseDto;
import pp.coinwash.history.domain.entity.History;
import pp.coinwash.history.domain.repository.HistoryRepository;

@Service
@RequiredArgsConstructor
public class HistoryService {

	private final HistoryRepository historyRepository;

	public void createUsageHistory(HistoryRequestDto requestDto
		, Machine machine) {

		historyRepository.save(History.of(requestDto, machine));
	}

	//TODO 추후 QueryDSL 을 이용하여 조회할 수 있도록 함.
	public PagedResponseDto<HistoryResponseDto> getUsageHistoriesByCustomerId(
		long customerId, Pageable pageable) {

		return PagedResponseDto.from(historyRepository
			.findAllByCustomerId(customerId, pageable)
			.map(HistoryResponseDto::from));
	}


}
