package pp.coinwash.notification.domain.dto.repository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import pp.coinwash.notification.domain.repository.SseEmitterRepository;

@ExtendWith(MockitoExtension.class)
class SseEmitterRepositoryTest {

	@InjectMocks
	private SseEmitterRepository sseEmitterRepository;

	private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

	private Long customerId;
	private SseEmitter sseEmitter;

	@BeforeEach
	void setUp() {
		customerId = 1L;
		sseEmitter = new SseEmitter(30000L); // 30초 타임아웃

		// 실제 Map을 사용하는 경우
		sseEmitterRepository = new SseEmitterRepository();
	}

	@Test
	@DisplayName("SSE Emitter 저장 성공 테스트")
	void save_Success() {
		// when
		SseEmitter result = sseEmitterRepository.save(customerId, sseEmitter);

		// then
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(sseEmitter);

		// emitters Map에 저장되었는지 확인
		assertThat(sseEmitterRepository.findById(customerId)).isPresent();
	}

	@Test
	@DisplayName("SSE 연결 완료 시 자동 삭제 테스트")
	void onCompletion_AutoDelete() {
		// given
		SseEmitter mockEmitter = mock(SseEmitter.class);
		ArgumentCaptor<Runnable> completionCaptor = ArgumentCaptor.forClass(Runnable.class);

		// when - save 호출 시 onCompletion 콜백 등록
		sseEmitterRepository.save(customerId, mockEmitter);

		// then - onCompletion 콜백이 등록되었는지 확인
		verify(mockEmitter).onCompletion(completionCaptor.capture());

		// 저장된 상태 확인
		assertThat(sseEmitterRepository.findById(customerId)).isPresent();

		// when - 캡처한 completion 콜백 실행
		Runnable completionCallback = completionCaptor.getValue();
		completionCallback.run();

		// then - 삭제되었는지 확인
		assertThat(sseEmitterRepository.findById(customerId)).isEmpty();
	}

	@Test
	@DisplayName("SSE 연결 타임아웃 시 자동 삭제 테스트")
	void onTimeout_AutoDelete() {
		// given
		SseEmitter mockEmitter = mock(SseEmitter.class);
		ArgumentCaptor<Runnable> timeoutCaptor = ArgumentCaptor.forClass(Runnable.class);

		// when - save 호출 시 onTimeout 콜백 등록
		sseEmitterRepository.save(customerId, mockEmitter);

		// then - onTimeout 콜백이 등록되었는지 확인
		verify(mockEmitter).onTimeout(timeoutCaptor.capture());

		// 저장된 상태 확인
		assertThat(sseEmitterRepository.findById(customerId)).isPresent();

		// when - 캡처한 timeout 콜백 실행
		Runnable timeoutCallback = timeoutCaptor.getValue();
		timeoutCallback.run();

		// then - 삭제되었는지 확인
		assertThat(sseEmitterRepository.findById(customerId)).isEmpty();
	}

	@Test
	@DisplayName("SSE 연결 오류 시 자동 삭제 테스트")
	void onError_AutoDelete() throws Exception {
		// given
		SseEmitter mockEmitter = mock(SseEmitter.class);
		ArgumentCaptor<Consumer<Throwable>> errorCaptor = ArgumentCaptor.forClass(Consumer.class);

		// when - save 호출 시 onError 콜백 등록
		sseEmitterRepository.save(customerId, mockEmitter);

		// then - onError 콜백이 등록되었는지 확인
		verify(mockEmitter).onError(errorCaptor.capture());

		// 저장된 상태 확인
		assertThat(sseEmitterRepository.findById(customerId)).isPresent();

		// when - 캡처한 error 콜백 실행
		Consumer<Throwable> errorCallback = errorCaptor.getValue();
		errorCallback.accept(new RuntimeException("Connection error"));

		// then - 삭제되었는지 확인
		assertThat(sseEmitterRepository.findById(customerId)).isEmpty();
	}

	@Test
	@DisplayName("동일한 고객 ID로 중복 저장 시 덮어쓰기 테스트")
	void save_DuplicateCustomerId_Overwrite() {
		// given
		SseEmitter firstEmitter = new SseEmitter();
		SseEmitter secondEmitter = new SseEmitter();

		// when
		sseEmitterRepository.save(customerId, firstEmitter);
		sseEmitterRepository.save(customerId, secondEmitter);

		// then
		Optional<SseEmitter> result = sseEmitterRepository.findById(customerId);
		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(secondEmitter);
		assertThat(result.get()).isNotEqualTo(firstEmitter);
	}

}