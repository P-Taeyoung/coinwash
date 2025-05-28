package pp.coinwash.history.service;

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
import pp.coinwash.history.domain.dto.HistoryRequestDto;
import pp.coinwash.history.domain.dto.HistoryResponseDto;
import pp.coinwash.history.domain.entity.History;
import pp.coinwash.history.domain.repository.HistoryRepository;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

	@Mock
	private HistoryRepository historyRepository;

	@InjectMocks
	private HistoryService historyService;

	private History history1;
	private HistoryResponseDto responseDto;
	private HistoryRequestDto requestDto;
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


		history1 = History.builder()
			.historyId(1)
			.machine(machine)
			.customerId(1)
			.build();

		requestDto = HistoryRequestDto.builder()
			.customerId(1)
			.build();
	}

	@DisplayName("사용 내역 생성")
	@Test
	void createUsageHistory() {
		//given
		//when
		historyService.createUsageHistory(requestDto, machine);

		//then
		ArgumentCaptor<History> captor = ArgumentCaptor.forClass(History.class);
		verify(historyRepository, times(1)).save(captor.capture());

		History history = captor.getValue();
		assertThat(history.getCustomerId()).isEqualTo(1);
		assertThat(history.getMachine().getMachineId()).isEqualTo(2);
		assertThat(history.getMachine().getLaundry().getLaundryId()).isEqualTo(3);
	}

	@DisplayName("사용 내역 조회")
	@Test
	void getUsageHistoriesByCustomerId() {
		//given
		long customerId = 1;

		History history2 = History.builder()
			.historyId(2)
			.machine(machine)
			.customerId(2)
			.build();
		History history3 = History.builder()
			.historyId(3)
			.machine(machine)
			.customerId(2)
			.build();

		List<History> usageHistories = List.of(history1, history2, history3);
		Page<History> historyPage = new PageImpl<>(usageHistories, pageable, usageHistories.size());
		when(historyRepository.findAllByCustomerId(customerId, pageable))
			.thenReturn(historyPage);

		//when
		PagedResponseDto<HistoryResponseDto> result =
			historyService.getUsageHistoriesByCustomerId(customerId, pageable);

		//then
		verify(historyRepository, times(1)).findAllByCustomerId(customerId, pageable);
		assertThat(PagedResponseDto.from(historyPage.map(HistoryResponseDto::from))).isEqualTo(result);
		assertThat(usageHistories.size()).isEqualTo(3);
		assertThat(usageHistories.get(2).getCustomerId()).isEqualTo(2);
	}
}