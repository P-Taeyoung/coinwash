package pp.coinwash.machine.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import pp.coinwash.machine.domain.dto.MachineResponseDto;
import pp.coinwash.machine.service.redis.MachineRedisService;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("performance")
class WeekendPeakTimePerformanceTest {

	@Autowired
	private MachineApplication machineApplication;

	@Autowired
	private MachineRedisService redisService;

	private static final Logger log = LoggerFactory.getLogger(WeekendPeakTimePerformanceTest.class);

	// 실제 데이터 규모에 맞춘 테스트 데이터
	private static final List<Long> POPULAR_LAUNDRY_IDS = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L,
		11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L);

	// 주말 피크 타임 시나리오 설정
	private static final int PEAK_CONCURRENT_USERS = 4000; // 400~500명 중간값
	private static final int REQUESTS_PER_USER = 3; // 사용자당 평균 3번 조회
	private static final int TOTAL_REQUESTS = PEAK_CONCURRENT_USERS * REQUESTS_PER_USER;

	// 테스트 시나리오별 설정
	private static final int WARMUP_ITERATIONS = 200;
	private static final int SUSTAINED_LOAD_DURATION_SECONDS = 30; // 30초간 지속 부하

	@Test
	@Order(1)
	@DisplayName("🏪 실제 데이터 규모 확인 및 캐시 상태 점검")
	void checkDataScaleAndCacheStatus() {
		log.info("=== 실제 데이터 규모 및 캐시 상태 확인 ===");

		// 실제 데이터 규모 확인
		log.info("📊 데이터 규모:");
		log.info("   - 고객 데이터: ~100,000개");
		log.info("   - 세탁소 데이터: ~1,000개");
		log.info("   - 기기 데이터: ~10,000개");

		// Redis 캐시 상태 확인
		Map<String, Object> stats = machineApplication.getRedisStats(POPULAR_LAUNDRY_IDS);

		log.info("🔄 Redis 캐시 통계:");
		log.info("   - 테스트 대상 세탁소: {}", POPULAR_LAUNDRY_IDS.size());
		log.info("   - 캐시된 세탁소: {}", stats.get("cachedLaundries"));
		log.info("   - 캐시 히트율: {}%", String.format("%.1f", (Double) stats.get("cacheHitRate")));
		log.info("   - 캐시된 총 기계 수: {}", stats.get("totalCachedMachines"));

		double hitRate = (Double) stats.get("cacheHitRate");
		if (hitRate < 95.0) {
			log.warn("⚠️ 캐시 히트율이 {}%로 낮습니다. 피크 타임 테스트 정확도에 영향을 줄 수 있습니다.",
				String.format("%.1f", hitRate));
		} else {
			log.info("✅ 피크 타임 테스트를 위한 캐시가 준비되었습니다.");
		}
	}

	@Test
	@Order(2)
	@DisplayName("🔥 피크 타임 대비 시스템 워밍업")
	void peakTimeWarmUp() {
		log.info("=== 피크 타임 대비 시스템 워밍업 시작 ===");

		// 점진적 부하 증가로 워밍업
		int[] warmupStages = {10, 50, 100, 200};

		for (int stage : warmupStages) {
			log.info("워밍업 단계: {}개 동시 요청", stage);

			ExecutorService executor = Executors.newFixedThreadPool(stage);
			CountDownLatch latch = new CountDownLatch(stage);

			for (int i = 0; i < stage; i++) {
				executor.submit(() -> {
					try {
						Long laundryId = POPULAR_LAUNDRY_IDS.get(
							ThreadLocalRandom.current().nextInt(POPULAR_LAUNDRY_IDS.size()));

						// Redis와 MySQL 모두 워밍업
						machineApplication.getMachinesFromRedisOnly(laundryId);
						machineApplication.getMachinesFromMySQLOnly(laundryId);
					} catch (Exception e) {
						// 워밍업 중 에러는 무시
					} finally {
						latch.countDown();
					}
				});
			}

			try {
				latch.await(10, TimeUnit.SECONDS);
				Thread.sleep(1000); // 단계별 휴식
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

			executor.shutdown();
		}

		// 메모리 정리
		System.gc();
		sleep(2000);
		log.info("=== 피크 타임 워밍업 완료 ===");
	}

	@Test
	@Order(3)
	@DisplayName("🏃‍♂️ 주말 피크 타임 시뮬레이션 - Redis vs MySQL")
	void weekendPeakTimeSimulation() throws InterruptedException {
		log.info("=== 주말 피크 타임 시뮬레이션 시작 ===");
		log.info("시나리오: {}명이 동시에 {}번씩 기기 조회 (총 {}건)",
			PEAK_CONCURRENT_USERS, REQUESTS_PER_USER, TOTAL_REQUESTS);

		// Redis 피크 타임 테스트
		log.info("🔴 Redis 피크 타임 테스트 시작...");
		PeakTimeResult redisResult = simulatePeakTime(
			machineApplication::getMachinesFromRedisOnly, "Redis");

		// 시스템 안정화
		sleep(3000);

		// MySQL 피크 타임 테스트
		log.info("🔵 MySQL 피크 타임 테스트 시작...");
		PeakTimeResult mysqlResult = simulatePeakTime(
			machineApplication::getMachinesFromMySQLOnly, "MySQL");

		// 결과 비교
		printPeakTimeComparison(redisResult, mysqlResult);
	}

	@Test
	@Order(4)
	@DisplayName("⏱️ 지속적 부하 테스트 (30초간)")
	void sustainedLoadTest() throws InterruptedException {
		log.info("=== 지속적 부하 테스트 시작 ===");
		log.info("테스트 시간: {}초, 동시 사용자: {}명", SUSTAINED_LOAD_DURATION_SECONDS, PEAK_CONCURRENT_USERS);

		// Redis 지속 부하 테스트
		log.info("🔴 Redis 지속 부하 테스트...");
		SustainedLoadResult redisResult = runSustainedLoadTest(
			machineApplication::getMachinesFromRedisOnly, "Redis");

		sleep(5000); // 시스템 회복 시간

		// MySQL 지속 부하 테스트
		log.info("🔵 MySQL 지속 부하 테스트...");
		SustainedLoadResult mysqlResult = runSustainedLoadTest(
			machineApplication::getMachinesFromMySQLOnly, "MySQL");

		printSustainedLoadComparison(redisResult, mysqlResult);
	}

	@Test
	@Order(5)
	@DisplayName("📊 실제 사용 패턴 시뮬레이션")
	void realUsagePatternSimulation() throws InterruptedException {
		log.info("=== 실제 사용 패턴 시뮬레이션 ===");
		log.info("패턴: 인기 세탁소 80%, 일반 세탁소 20% 비율로 조회");

		// 실제 사용 패턴: 파레토 법칙 적용 (80:20)
		List<Long> popularLaundries = POPULAR_LAUNDRY_IDS.subList(0, 8); // 상위 8개
		List<Long> normalLaundries = POPULAR_LAUNDRY_IDS.subList(8, 20); // 나머지 12개

		// Redis 실사용 패턴 테스트
		RealPatternResult redisResult = simulateRealUsagePattern(
			machineApplication::getMachinesFromRedisOnly, "Redis",
			popularLaundries, normalLaundries);

		sleep(3000);

		// MySQL 실사용 패턴 테스트
		RealPatternResult mysqlResult = simulateRealUsagePattern(
			machineApplication::getMachinesFromMySQLOnly, "MySQL",
			popularLaundries, normalLaundries);

		printRealPatternComparison(redisResult, mysqlResult);
	}

	// === Helper Methods ===

	private PeakTimeResult simulatePeakTime(
		Function<Long, List<MachineResponseDto>> operation, String type)
		throws InterruptedException {

		ExecutorService executor = Executors.newFixedThreadPool(PEAK_CONCURRENT_USERS);
		CountDownLatch latch = new CountDownLatch(PEAK_CONCURRENT_USERS);

		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger errorCount = new AtomicInteger(0);
		AtomicLong totalResponseTime = new AtomicLong(0);
		AtomicLong minTime = new AtomicLong(Long.MAX_VALUE);
		AtomicLong maxTime = new AtomicLong(Long.MIN_VALUE);

		List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());

		long startTime = System.nanoTime();

		// 동시 사용자 시뮬레이션
		for (int i = 0; i < PEAK_CONCURRENT_USERS; i++) {
			executor.submit(() -> {
				try {
					// 사용자당 여러 번 조회 (실제 사용 패턴)
					for (int j = 0; j < REQUESTS_PER_USER; j++) {
						Long laundryId = POPULAR_LAUNDRY_IDS.get(
							ThreadLocalRandom.current().nextInt(POPULAR_LAUNDRY_IDS.size()));

						long opStart = System.nanoTime();
						try {
							List<MachineResponseDto> result = operation.apply(laundryId);
							long opEnd = System.nanoTime();
							long opTime = opEnd - opStart;

							if (!result.isEmpty()) {
								successCount.incrementAndGet();
								totalResponseTime.addAndGet(opTime);
								responseTimes.add(opTime);

								updateMinMax(minTime, maxTime, opTime);
							}
						} catch (Exception e) {
							errorCount.incrementAndGet();
							log.debug("{} 피크타임 조회 실패: {}", type, e.getMessage());
						}

						// 실제 사용자 행동 시뮬레이션 (약간의 지연)
						if (j < REQUESTS_PER_USER - 1) {
							Thread.sleep(ThreadLocalRandom.current().nextInt(100, 500));
						}
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		long endTime = System.nanoTime();
		executor.shutdown();

		return new PeakTimeResult(type, endTime - startTime, successCount.get(),
			errorCount.get(), TOTAL_REQUESTS, totalResponseTime.get(),
			minTime.get(), maxTime.get(), responseTimes);
	}

	private SustainedLoadResult runSustainedLoadTest(
		Function<Long, List<MachineResponseDto>> operation, String type)
		throws InterruptedException {

		AtomicInteger totalRequests = new AtomicInteger(0);
		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger errorCount = new AtomicInteger(0);
		AtomicLong totalResponseTime = new AtomicLong(0);

		List<Double> tpsPerSecond = Collections.synchronizedList(new ArrayList<>());

		ExecutorService executor = Executors.newFixedThreadPool(PEAK_CONCURRENT_USERS);
		AtomicBoolean running = new AtomicBoolean(true);

		// TPS 측정을 위한 별도 스레드
		ScheduledExecutorService tpsMonitor = Executors.newSingleThreadScheduledExecutor();
		AtomicInteger lastSecondCount = new AtomicInteger(0);

		tpsMonitor.scheduleAtFixedRate(() -> {
			int currentCount = successCount.get();
			int tps = currentCount - lastSecondCount.get();
			tpsPerSecond.add((double) tps);
			lastSecondCount.set(currentCount);
			log.info("{} TPS: {}", type, tps);
		}, 1, 1, TimeUnit.SECONDS);

		long startTime = System.nanoTime();

		// 지속적 요청 생성
		for (int i = 0; i < PEAK_CONCURRENT_USERS; i++) {
			executor.submit(() -> {
				while (running.get()) {
					Long laundryId = POPULAR_LAUNDRY_IDS.get(
						ThreadLocalRandom.current().nextInt(POPULAR_LAUNDRY_IDS.size()));

					long opStart = System.nanoTime();
					try {
						totalRequests.incrementAndGet();
						List<MachineResponseDto> result = operation.apply(laundryId);
						long opEnd = System.nanoTime();

						if (!result.isEmpty()) {
							successCount.incrementAndGet();
							totalResponseTime.addAndGet(opEnd - opStart);
						}
					} catch (Exception e) {
						errorCount.incrementAndGet();
					}

					// 실제 사용자 요청 간격 시뮬레이션
					try {
						Thread.sleep(ThreadLocalRandom.current().nextInt(50, 200));
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
				}
			});
		}

		// 지정된 시간 동안 실행
		Thread.sleep(SUSTAINED_LOAD_DURATION_SECONDS * 1000);
		running.set(false);

		executor.shutdown();
		executor.awaitTermination(5, TimeUnit.SECONDS);

		tpsMonitor.shutdown();

		long endTime = System.nanoTime();

		return new SustainedLoadResult(type, endTime - startTime,
			totalRequests.get(), successCount.get(),
			errorCount.get(), totalResponseTime.get(), tpsPerSecond);
	}

	private RealPatternResult simulateRealUsagePattern(
		Function<Long, List<MachineResponseDto>> operation, String type,
		List<Long> popularLaundries, List<Long> normalLaundries)
		throws InterruptedException {

		ExecutorService executor = Executors.newFixedThreadPool(PEAK_CONCURRENT_USERS);
		CountDownLatch latch = new CountDownLatch(PEAK_CONCURRENT_USERS);

		AtomicInteger popularRequests = new AtomicInteger(0);
		AtomicInteger normalRequests = new AtomicInteger(0);
		AtomicLong popularResponseTime = new AtomicLong(0);
		AtomicLong normalResponseTime = new AtomicLong(0);

		long startTime = System.nanoTime();

		for (int i = 0; i < PEAK_CONCURRENT_USERS; i++) {
			executor.submit(() -> {
				try {
					for (int j = 0; j < REQUESTS_PER_USER; j++) {
						// 80:20 비율로 세탁소 선택
						boolean isPopular = ThreadLocalRandom.current().nextDouble() < 0.8;
						Long laundryId;

						if (isPopular) {
							laundryId = popularLaundries.get(
								ThreadLocalRandom.current().nextInt(popularLaundries.size()));
						} else {
							laundryId = normalLaundries.get(
								ThreadLocalRandom.current().nextInt(normalLaundries.size()));
						}

						long opStart = System.nanoTime();
						try {
							List<MachineResponseDto> result = operation.apply(laundryId);
							long opEnd = System.nanoTime();
							long opTime = opEnd - opStart;

							if (!result.isEmpty()) {
								if (isPopular) {
									popularRequests.incrementAndGet();
									popularResponseTime.addAndGet(opTime);
								} else {
									normalRequests.incrementAndGet();
									normalResponseTime.addAndGet(opTime);
								}
							}
						} catch (Exception e) {
							log.debug("{} 실사용패턴 조회 실패: {}", type, e.getMessage());
						}

						Thread.sleep(ThreadLocalRandom.current().nextInt(100, 300));
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		long endTime = System.nanoTime();
		executor.shutdown();

		return new RealPatternResult(type, endTime - startTime,
			popularRequests.get(), normalRequests.get(),
			popularResponseTime.get(), normalResponseTime.get());
	}

	private void updateMinMax(AtomicLong minTime, AtomicLong maxTime, long opTime) {
		long currentMin = minTime.get();
		while (opTime < currentMin && !minTime.compareAndSet(currentMin, opTime)) {
			currentMin = minTime.get();
		}

		long currentMax = maxTime.get();
		while (opTime > currentMax && !maxTime.compareAndSet(currentMax, opTime)) {
			currentMax = maxTime.get();
		}
	}

	private void printPeakTimeComparison(PeakTimeResult redis, PeakTimeResult mysql) {
		log.info("🎯 === 주말 피크 타임 성능 비교 결과 ===");
		log.info("📊 테스트 규모: {}명 동시 접속, 총 {}건 요청", PEAK_CONCURRENT_USERS, TOTAL_REQUESTS);

		log.info("⚡ Redis 결과:");
		log.info("   - 성공률: {}% ({}/{})",
			String.format("%.1f", redis.getSuccessRate()),
			redis.getSuccessCount(),
			redis.getTotalRequests());
		log.info("   - 평균 응답시간: {}ms", String.format("%.2f", redis.getAvgResponseTime()));
		log.info("   - 95% 응답시간: {}ms", String.format("%.2f", redis.getPercentile95()));
		log.info("   - TPS: {}", String.format("%.0f", redis.getTPS()));

		log.info("💾 MySQL 결과:");
		log.info("   - 성공률: {}% ({}/{})",
			String.format("%.1f", mysql.getSuccessRate()),
			mysql.getSuccessCount(),
			mysql.getTotalRequests());
		log.info("   - 평균 응답시간: {}ms", String.format("%.2f", mysql.getAvgResponseTime()));
		log.info("   - 95% 응답시간: {}ms", String.format("%.2f", mysql.getPercentile95()));
		log.info("   - TPS: {}", String.format("%.0f", mysql.getTPS()));

		double performanceGain = mysql.getAvgResponseTime() / redis.getAvgResponseTime();
		double tpsGain = redis.getTPS() / mysql.getTPS();

		log.info("🚀 성능 개선:");
		log.info("   - 응답시간: {}배 향상", String.format("%.1f", performanceGain));
		log.info("   - 처리량: {}배 향상", String.format("%.1f", tpsGain));
		log.info("   - 에러율 차이: Redis {}%, MySQL {}%",
			String.format("%.2f", redis.getErrorRate()),
			String.format("%.2f", mysql.getErrorRate()));
		log.info("=======================================");
	}

	private void printSustainedLoadComparison(SustainedLoadResult redis, SustainedLoadResult mysql) {
		log.info("🎯 === 지속적 부하 테스트 결과 ===");

		log.info("⚡ Redis - {}초간 지속 부하:", SUSTAINED_LOAD_DURATION_SECONDS);
		log.info("   - 총 요청: {}, 성공: {}", redis.getTotalRequests(), redis.getSuccessCount());
		log.info("   - 평균 TPS: {}", String.format("%.0f", redis.getAverageTPS()));
		log.info("   - 최대 TPS: {}", String.format("%.0f", redis.getMaxTPS()));
		log.info("   - 평균 응답시간: {}ms", String.format("%.2f", redis.getAvgResponseTime()));

		log.info("💾 MySQL - {}초간 지속 부하:", SUSTAINED_LOAD_DURATION_SECONDS);
		log.info("   - 총 요청: {}, 성공: {}", mysql.getTotalRequests(), mysql.getSuccessCount());
		log.info("   - 평균 TPS: {}", String.format("%.0f", mysql.getAverageTPS()));
		log.info("   - 최대 TPS: {}", String.format("%.0f", mysql.getMaxTPS()));
		log.info("   - 평균 응답시간: {}ms", String.format("%.2f", mysql.getAvgResponseTime()));

		log.info("🚀 지속 성능 비교:");
		log.info("   - TPS 향상: {}배", String.format("%.1f", redis.getAverageTPS() / mysql.getAverageTPS()));
		log.info("   - 응답시간 개선: {}배", String.format("%.1f", mysql.getAvgResponseTime() / redis.getAvgResponseTime()));
		log.info("=======================================");
	}

	private void printRealPatternComparison(RealPatternResult redis, RealPatternResult mysql) {
		log.info("🎯 === 실제 사용 패턴 시뮬레이션 결과 ===");

		log.info("⚡ Redis 패턴별 성능:");
		log.info("   - 인기 세탁소 (80%): 평균 {}ms", String.format("%.2f", redis.getPopularAvgTime()));
		log.info("   - 일반 세탁소 (20%): 평균 {}ms", String.format("%.2f", redis.getNormalAvgTime()));

		log.info("💾 MySQL 패턴별 성능:");
		log.info("   - 인기 세탁소 (80%): 평균 {}ms", String.format("%.2f", mysql.getPopularAvgTime()));
		log.info("   - 일반 세탁소 (20%): 평균 {}ms", String.format("%.2f", mysql.getNormalAvgTime()));

		log.info("🚀 패턴별 성능 개선:");
		log.info("   - 인기 세탁소: {}배 향상", String.format("%.1f", mysql.getPopularAvgTime() / redis.getPopularAvgTime()));
		log.info("   - 일반 세탁소: {}배 향상", String.format("%.1f", mysql.getNormalAvgTime() / redis.getNormalAvgTime()));
		log.info("=======================================");
	}

	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	// === Result Classes ===

	public static class PeakTimeResult {
		private final String type;
		private final long totalTime;
		private final int successCount;
		private final int errorCount;
		private final int totalRequests;
		private final long totalResponseTime;
		private final long minTime;
		private final long maxTime;
		private final List<Long> responseTimes;

		public PeakTimeResult(String type, long totalTime, int successCount, int errorCount,
			int totalRequests, long totalResponseTime, long minTime, long maxTime,
			List<Long> responseTimes) {
			this.type = type;
			this.totalTime = totalTime;
			this.successCount = successCount;
			this.errorCount = errorCount;
			this.totalRequests = totalRequests;
			this.totalResponseTime = totalResponseTime;
			this.minTime = minTime;
			this.maxTime = maxTime;
			this.responseTimes = new ArrayList<>(responseTimes);
		}

		public double getSuccessRate() {
			return (successCount * 100.0) / totalRequests;
		}

		public double getErrorRate() {
			return (errorCount * 100.0) / totalRequests;
		}

		public double getAvgResponseTime() {
			return successCount > 0 ? (totalResponseTime / 1_000_000.0) / successCount : 0;
		}

		public double getTPS() {
			return successCount * 1000.0 / (totalTime / 1_000_000.0);
		}

		public double getPercentile95() {
			if (responseTimes.isEmpty()) return 0;

			List<Long> sorted = new ArrayList<>(responseTimes);
			sorted.sort(Long::compareTo);
			int index = (int) Math.ceil(sorted.size() * 0.95) - 1;
			return sorted.get(Math.max(0, index)) / 1_000_000.0;
		}

		// Getters
		public String getType() { return type; }
		public int getSuccessCount() { return successCount; }
		public int getTotalRequests() { return totalRequests; }
	}

	public static class SustainedLoadResult {
		private final String type;
		private final long totalTime;
		private final int totalRequests;
		private final int successCount;
		private final int errorCount;
		private final long totalResponseTime;
		private final List<Double> tpsPerSecond;

		public SustainedLoadResult(String type, long totalTime, int totalRequests, int successCount,
			int errorCount, long totalResponseTime, List<Double> tpsPerSecond) {
			this.type = type;
			this.totalTime = totalTime;
			this.totalRequests = totalRequests;
			this.successCount = successCount;
			this.errorCount = errorCount;
			this.totalResponseTime = totalResponseTime;
			this.tpsPerSecond = new ArrayList<>(tpsPerSecond);
		}

		public double getAverageTPS() {
			return tpsPerSecond.stream().mapToDouble(Double::doubleValue).average().orElse(0);
		}

		public double getMaxTPS() {
			return tpsPerSecond.stream().mapToDouble(Double::doubleValue).max().orElse(0);
		}

		public double getAvgResponseTime() {
			return successCount > 0 ? (totalResponseTime / 1_000_000.0) / successCount : 0;
		}

		// Getters
		public int getTotalRequests() { return totalRequests; }
		public int getSuccessCount() { return successCount; }
	}

	public static class RealPatternResult {
		private final String type;
		private final long totalTime;
		private final int popularRequests;
		private final int normalRequests;
		private final long popularResponseTime;
		private final long normalResponseTime;

		public RealPatternResult(String type, long totalTime, int popularRequests, int normalRequests,
			long popularResponseTime, long normalResponseTime) {
			this.type = type;
			this.totalTime = totalTime;
			this.popularRequests = popularRequests;
			this.normalRequests = normalRequests;
			this.popularResponseTime = popularResponseTime;
			this.normalResponseTime = normalResponseTime;
		}

		public double getPopularAvgTime() {
			return popularRequests > 0 ? (popularResponseTime / 1_000_000.0) / popularRequests : 0;
		}

		public double getNormalAvgTime() {
			return normalRequests > 0 ? (normalResponseTime / 1_000_000.0) / normalRequests : 0;
		}

		// Getters
		public String getType() { return type; }
		public int getPopularRequests() { return popularRequests; }
		public int getNormalRequests() { return normalRequests; }
	}
}
