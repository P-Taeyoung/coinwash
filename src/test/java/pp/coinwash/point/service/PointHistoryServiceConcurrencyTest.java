package pp.coinwash.point.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import pp.coinwash.point.application.PointHistoryApplication;
import pp.coinwash.point.domain.dto.PointHistoryRequestDto;
import pp.coinwash.point.domain.repository.PointHistoryRepository;
import pp.coinwash.point.domain.type.PointType;
import pp.coinwash.user.domain.entity.Customer;
import pp.coinwash.user.domain.repository.CustomerRepository;

@SpringBootTest
@ActiveProfiles("test")
public class PointHistoryServiceConcurrencyTest {

	@Autowired
	private PointHistoryApplication pointHistoryApplication;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private PointHistoryRepository pointHistoryRepository;

	@Test
	void concurrencyControlTest() throws InterruptedException {
		//given
		Customer customer = Customer.builder()
			.name("테스트고객")
			.points(1000)
			.build();

		//when
		// 데이터를 실제 DB에 저장하고 트랜잭션 커밋
		customer = customerRepository.saveAndFlush(customer);

		final long customerId = customer.getCustomerId();

		System.out.println("Customer ID: " + customerId);
		System.out.println("Customer Version: " + customer.getVersion());

		int threadCount = 5;
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);

		List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());
		List<Boolean> results = Collections.synchronizedList(new ArrayList<>());

		for (int i = 0; i < threadCount; i++) {
			final int changedPoints = 200; // 각각 200포인트씩 사용
			final int threadNum = i;
			executorService.submit(() -> {
				try {
					System.out.println("Thread " + threadNum + " starting...");

					PointHistoryRequestDto dto = PointHistoryRequestDto.builder()
						.customerId(customerId)
						.changedPoint(changedPoints)
						.pointType(PointType.USED)
						.build();

					pointHistoryApplication.usePoints(dto);
					results.add(true);
					System.out.println("Thread " + threadNum + " SUCCESS");

				} catch (Exception e) {

					exceptions.add(e);
					results.add(false);
					System.out.println("Thread " + threadNum + " FAILED: " + e.getMessage());

				} finally {
					latch.countDown();
				}
			});
		}

		//then
		latch.await(10, TimeUnit.SECONDS);
		executorService.shutdown();

		// 성공한 요청은 1개만 있어야 함 (1000포인트에서 200포인트만 차감 가능)
		long successCount = results.stream().mapToLong(r -> r ? 1 : 0).sum();
		assertThat(successCount).isEqualTo(1);

		System.out.println("Success count: " + successCount);
		System.out.println("Exception count: " + exceptions.size());


		// 나머지는 낙관적 락 예외가 발생해야 함
		assertThat(exceptions.size()).isEqualTo(4);
		exceptions.forEach(e ->
			assertThat(e.getMessage()).contains("동시에 포인트 변동이 이뤄졌습니다. 다시 시도해주세요.")
		);

		// 최종 포인트 확인
		Customer finalCustomer = customerRepository.findById(customerId).orElseThrow();
		assertThat(finalCustomer.getPoints()).isEqualTo(800);
	}
}
