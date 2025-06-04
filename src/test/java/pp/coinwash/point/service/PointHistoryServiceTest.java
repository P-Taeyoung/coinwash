package pp.coinwash.point.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import pp.coinwash.common.dto.PagedResponseDto;
import pp.coinwash.history.domain.entity.History;
import pp.coinwash.point.domain.dto.PointHistoryRequestDto;
import pp.coinwash.point.domain.dto.PointHistoryResponseDto;
import pp.coinwash.point.domain.entity.PointHistory;
import pp.coinwash.point.domain.repository.PointHistoryRepository;
import pp.coinwash.point.domain.type.PointType;
import pp.coinwash.user.domain.entity.Customer;
import pp.coinwash.user.domain.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class PointHistoryServiceTest {

	@Mock
	private PointHistoryRepository pointHistoryRepository;

	@Mock
	private CustomerRepository customerRepository;

	@InjectMocks
	private PointHistoryService pointHistoryService;

	private Customer customer;
	private PointHistoryRequestDto requestDto;

	@BeforeEach
	void setUp() {
		customer = Customer.builder()
			.customerId(1)
			.points(10)
			.build();
	}

	@DisplayName("포인트 사용")
	@Test
	void usePoint() {
		//given
		requestDto = PointHistoryRequestDto.builder()
			.customerId(2)
			.changedPoint(5)
			.pointType(PointType.USED)
			.build();

		when(customerRepository.findValidateCustomerToUsePoints(requestDto.customerId()))
			.thenReturn(Optional.ofNullable(customer));

		//when
		pointHistoryService.usePoint(requestDto);

		//then
		ArgumentCaptor<PointHistory> captor = ArgumentCaptor.forClass(PointHistory.class);
		verify(pointHistoryRepository).save(captor.capture());
		PointHistory pointHistory = captor.getValue();
		assertEquals(PointType.USED, pointHistory.getPointType());
		assertEquals(requestDto.customerId(), pointHistory.getCustomerId());
		assertEquals(requestDto.changedPoint(), pointHistory.getChangedPoints());

		assertEquals(5, customer.getPoints());
	}

	@DisplayName("포인트 적립")
	@Test
	void earnPoint() {
		//given
		requestDto = PointHistoryRequestDto.builder()
			.customerId(2)
			.changedPoint(5)
			.pointType(PointType.EARNED)
			.build();

		when(customerRepository.findValidateCustomerToUsePoints(requestDto.customerId()))
			.thenReturn(Optional.ofNullable(customer));

		//when
		pointHistoryService.earnPoint(requestDto);

		//then
		ArgumentCaptor<PointHistory> captor = ArgumentCaptor.forClass(PointHistory.class);
		verify(pointHistoryRepository).save(captor.capture());
		PointHistory pointHistory = captor.getValue();
		assertEquals(PointType.EARNED, pointHistory.getPointType());
		assertEquals(requestDto.customerId(), pointHistory.getCustomerId());
		assertEquals(requestDto.changedPoint(), pointHistory.getChangedPoints());

		assertEquals(15, customer.getPoints());
	}

	@DisplayName("포인트 내역 조회")
	@Test
	void getPointHistory() {
		//given
		PointHistory pointHistory1 = PointHistory.builder()
			.customerId(1)
			.pointType(PointType.EARNED)
			.changedPoints(10)
			.build();

		PointHistory pointHistory2 = PointHistory.builder()
			.customerId(1)
			.pointType(PointType.USED)
			.changedPoints(3)
			.build();

		PointHistory pointHistory3 = PointHistory.builder()
			.customerId(1)
			.pointType(PointType.EARNED)
			.changedPoints(10)
			.build();


		Pageable pageable = PageRequest.of(0, 2);
		List<PointHistory> pointHistories = List.of(pointHistory1, pointHistory2, pointHistory3);
		Page<PointHistory> pointHistoryPage = new PageImpl<>(pointHistories, pageable, pointHistories.size());

		when(pointHistoryRepository.findAllByCustomerId(customer.getCustomerId(),pageable))
			.thenReturn(pointHistoryPage);

		//when
		Page<PointHistory> result =
			pointHistoryService.getPointHistory(customer.getCustomerId(),pageable);

		//then
		assertEquals(pointHistories.size(), result.getTotalElements());
		assertEquals(pointHistory2, result.getContent().get(1));
		assertEquals(2, result.getTotalPages());

	}

	@DisplayName("포인트 부족으로 인한 포인트 사용 실패")
	@Test
	void insufficientPoints() {
		//given
		PointHistoryRequestDto requestDto1 = PointHistoryRequestDto.builder()
			.customerId(2)
			.changedPoint(15)
			.pointType(PointType.USED)
			.build();

		when(customerRepository.findValidateCustomerToUsePoints(requestDto1.customerId()))
			.thenReturn(Optional.ofNullable(customer));

		//when
		//then
		RuntimeException exception = assertThrows(RuntimeException.class,
			() -> pointHistoryService.usePoint(requestDto1));

		assertEquals("포인트가 부족합니다.", exception.getMessage());
	}


}