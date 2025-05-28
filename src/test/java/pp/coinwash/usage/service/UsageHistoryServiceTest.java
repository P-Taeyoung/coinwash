package pp.coinwash.usage.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import pp.coinwash.common.dto.PagedResponseDto;
import pp.coinwash.laundry.domain.entity.Laundry;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.usage.domain.dto.UsageHistoryRequestDto;
import pp.coinwash.usage.domain.dto.UsageHistoryResponseDto;
import pp.coinwash.usage.domain.entity.UsageHistory;
import pp.coinwash.usage.domain.repository.UsageHistoryRepository;

@ExtendWith(MockitoExtension.class)
class UsageHistoryServiceTest {

	@Mock
	private UsageHistoryRepository usageHistoryRepository;

	@InjectMocks
	private UsageHistoryService usageHistoryService;

	private UsageHistory usageHistory1;
	private UsageHistoryResponseDto responseDto;
	private UsageHistoryRequestDto requestDto;
	private Pageable pageable;
	private Machine machine;
	private Laundry laundry;

	@BeforeEach
	void setUp() {

		pageable = PageRequest.of(0, 10);

		laundry = Laundry.builder()
			.laundryId(3)
			.addressName("동대문구 망우로")
			.build();

		machine = Machine.builder()
			.machineId(2)
			.laundry(laundry)
			.build();


		usageHistory1 = UsageHistory.builder()
			.historyId(1)
			.machine(machine)
			.customerId(1)
			.build();

		requestDto = UsageHistoryRequestDto.builder()
			.customerId(1)
			.build();
	}

	@DisplayName("사용 내역 생성")
	@Test
	void createUsageHistory() {
		//given
		//when
		usageHistoryService.createUsageHistory(requestDto, machine);

		//then
		ArgumentCaptor<UsageHistory> captor = ArgumentCaptor.forClass(UsageHistory.class);
		verify(usageHistoryRepository, times(1)).save(captor.capture());

		UsageHistory usageHistory = captor.getValue();
		assertThat(usageHistory.getCustomerId()).isEqualTo(1);
		assertThat(usageHistory.getMachine().getMachineId()).isEqualTo(2);
		assertThat(usageHistory.getMachine().getLaundry().getLaundryId()).isEqualTo(3);
	}

	@DisplayName("사용 내역 조회")
	@Test
	void getUsageHistoriesByCustomerId() {
		//given
		long customerId = 1;

		UsageHistory usageHistory2 = UsageHistory.builder()
			.historyId(2)
			.machine(machine)
			.customerId(2)
			.build();
		UsageHistory usageHistory3 = UsageHistory.builder()
			.historyId(3)
			.machine(machine)
			.customerId(2)
			.build();

		List<UsageHistory> usageHistories = List.of(usageHistory1, usageHistory2, usageHistory3);
		Page<UsageHistory> historyPage = new PageImpl<>(usageHistories, pageable, usageHistories.size());
		when(usageHistoryRepository.findAllByCustomerId(customerId, pageable))
			.thenReturn(historyPage);

		//when
		PagedResponseDto<UsageHistoryResponseDto> result =
			usageHistoryService.getUsageHistoriesByCustomerId(customerId, pageable);

		//then
		verify(usageHistoryRepository, times(1)).findAllByCustomerId(customerId, pageable);
		assertThat(PagedResponseDto.from(historyPage.map(UsageHistoryResponseDto::from))).isEqualTo(result);
		assertThat(usageHistories.size()).isEqualTo(3);
		assertThat(usageHistories.get(2).getCustomerId()).isEqualTo(2);
	}
}