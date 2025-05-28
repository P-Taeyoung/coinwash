package pp.coinwash.usage.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pp.coinwash.common.dto.PagedResponseDto;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.usage.domain.dto.UsageHistoryRequestDto;
import pp.coinwash.usage.domain.dto.UsageHistoryResponseDto;
import pp.coinwash.usage.domain.entity.UsageHistory;
import pp.coinwash.usage.domain.repository.UsageHistoryRepository;

@Service
@RequiredArgsConstructor
public class UsageHistoryService {

	private final UsageHistoryRepository usageHistoryRepository;

	public void createUsageHistory(UsageHistoryRequestDto requestDto
		, Machine machine) {

		usageHistoryRepository.save(UsageHistory.of(requestDto, machine));
	}

	//TODO 추후 QueryDSL 을 이용하여 조회할 수 있도록 함.
	public PagedResponseDto<UsageHistoryResponseDto> getUsageHistoriesByCustomerId(
		long customerId, Pageable pageable) {

		return PagedResponseDto.from(usageHistoryRepository
			.findAllByCustomerId(customerId, pageable)
			.map(UsageHistoryResponseDto::from));
	}


}
