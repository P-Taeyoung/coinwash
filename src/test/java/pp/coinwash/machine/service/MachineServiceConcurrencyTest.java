package pp.coinwash.machine.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static pp.coinwash.machine.domain.type.MachineType.*;
import static pp.coinwash.machine.domain.type.UsageStatus.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import pp.coinwash.history.domain.type.WashingCourse;
import pp.coinwash.machine.domain.dto.UsingWashingDto;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.domain.repository.MachineRepository;
import pp.coinwash.machine.domain.type.UsageStatus;
import pp.coinwash.user.domain.entity.Customer;
import pp.coinwash.user.domain.repository.CustomerRepository;

@SpringBootTest
@ActiveProfiles("test")
public class MachineServiceConcurrencyTest {

	@Autowired
	private UsingMachineService machineService;

	@Autowired
	private ReservingMachineService reservingMachineService;

	@Autowired
	private MachineRepository machineRepository;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private PlatformTransactionManager transactionManager;

	private Machine machine;
	private UsingWashingDto washingDto;
	private Customer customer1;
	private Customer customer2;

	@BeforeEach
	public void init() {

		machine = Machine.builder()
			.machineType(WASHING)
			.usageStatus(USABLE)
			.build();

		washingDto = UsingWashingDto.builder()
			.machineId(1)
			.course(WashingCourse.WASHING_A_COURSE)
			.build();

		customer1 = Customer.builder()
			.points(500)
			.name("홍길동")
			.build();

		customer2 = Customer.builder()
			.points(500)
			.name("이순신")
			.build();

	}

	@Test
	@DisplayName("동시에 같은 세탁기를 사용하려고 할 때 한 명만 성공해야 한다")
	void testConcurrentWashingUsage() throws InterruptedException {
		// Given

		machineRepository.save(machine);
		customer1 = customerRepository.save(customer1);
		customer2 = customerRepository.save(customer2);

		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch endLatch = new CountDownLatch(2);

		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failCount = new AtomicInteger(0);
		List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

		// Thread 1: Customer 1이 세탁기 사용
		Thread thread1 = new Thread(() -> {
			TransactionTemplate template = new TransactionTemplate(transactionManager);
			try {
				startLatch.await();

				template.execute(status -> {
					try {
						machineService.useWashing(customer1.getCustomerId(), washingDto);
						successCount.incrementAndGet();
						System.out.println("Customer 1 성공");
					} catch (Exception e) {
						failCount.incrementAndGet();
						exceptions.add(e);
						System.out.println("Customer 1 실패: " + e.getMessage());
					}
					return null;
				});
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} finally {
				endLatch.countDown();
			}
		});

		// Thread 2: Customer 2가 같은 세탁기 사용
		Thread thread2 = new Thread(() -> {
			TransactionTemplate template = new TransactionTemplate(transactionManager);
			try {
				startLatch.await();
				Thread.sleep(10); // 약간의 지연으로 경합 상황 만들기

				template.execute(status -> {
					try {
						machineService.useWashing(customer2.getCustomerId(), washingDto);
						successCount.incrementAndGet();
						System.out.println("Customer 2 성공");
					} catch (Exception e) {
						failCount.incrementAndGet();
						exceptions.add(e);
						System.out.println("Customer 2 실패: " + e.getMessage());
					}
					return null;
				});
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} finally {
				endLatch.countDown();
			}
		});

		// When
		thread1.start();
		thread2.start();
		startLatch.countDown(); // 동시 시작

		boolean completed = endLatch.await(10, TimeUnit.SECONDS);

		// Then
		assertThat(completed).isTrue();
		assertThat(successCount.get()).isEqualTo(1); // 한 명만 성공
		assertThat(failCount.get()).isEqualTo(1);    // 한 명은 실패

		// 실제 DB 상태 확인
		Machine updatedMachine = machineRepository.findById(machine.getMachineId()).orElseThrow();
		assertThat(updatedMachine.getUsageStatus()).isEqualTo(UsageStatus.USING);

		System.out.println("성공: " + successCount.get() + ", 실패: " + failCount.get());
		exceptions.forEach(e -> System.out.println("예외: " + e.getMessage()));
	}

	@Test
	@DisplayName("사용, 예약 동시에 하는 경우에 하나만 성공")
	void testConcurrentGettingMachine() throws InterruptedException {
		// Given

		machineRepository.save(machine);
		customer1 = customerRepository.save(customer1);
		customer2 = customerRepository.save(customer2);

		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch endLatch = new CountDownLatch(2);

		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failCount = new AtomicInteger(0);
		List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

		// Thread 1: Customer 1이 세탁기 사용
		Thread thread1 = new Thread(() -> {
			TransactionTemplate template = new TransactionTemplate(transactionManager);
			try {
				startLatch.await();

				template.execute(status -> {
					try {
						machineService.useWashing(customer1.getCustomerId(), washingDto);
						successCount.incrementAndGet();
						System.out.println("Customer 1 성공");
					} catch (Exception e) {
						failCount.incrementAndGet();
						exceptions.add(e);
						System.out.println("Customer 1 실패: " + e.getMessage());
					}
					return null;
				});
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} finally {
				endLatch.countDown();
			}
		});

		// Thread 2: Customer 2가 같은 세탁기 조회
		Thread thread2 = new Thread(() -> {
			TransactionTemplate template = new TransactionTemplate(transactionManager);
			try {
				startLatch.await();
				Thread.sleep(10); // 약간의 지연으로 경합 상황 만들기

				template.execute(status -> {
					try {
						reservingMachineService.reserveMachine(washingDto.machineId(), customer1.getCustomerId());
						successCount.incrementAndGet();
						System.out.println("Customer 2 성공");
					} catch (Exception e) {
						failCount.incrementAndGet();
						exceptions.add(e);
						System.out.println("Customer 2 실패: " + e.getMessage());
					}
					return null;
				});
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} finally {
				endLatch.countDown();
			}
		});

		// When
		thread1.start();
		thread2.start();
		startLatch.countDown(); // 동시 시작

		boolean completed = endLatch.await(10, TimeUnit.SECONDS);

		// Then
		assertThat(completed).isTrue();
		assertThat(successCount.get()).isEqualTo(1); // 한 명만 성공

		// 실제 DB 상태 확인
		Machine updatedMachine = machineRepository.findById(machine.getMachineId()).orElseThrow();
		assertThat(updatedMachine.getUsageStatus()).isEqualTo(UsageStatus.USING);
		System.out.println("기계 Id : " + machine.getMachineId());
		System.out.println("기계 상태 : " + updatedMachine.getUsageStatus());

		System.out.println("성공: " + successCount.get() + ", 실패: " + failCount.get());
		exceptions.forEach(e -> System.out.println("예외: " + e.getMessage()));
	}
}
