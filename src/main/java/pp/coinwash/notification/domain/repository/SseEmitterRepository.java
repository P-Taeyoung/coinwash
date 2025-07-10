package pp.coinwash.notification.domain.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Repository
@Slf4j
public class SseEmitterRepository {
	private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

	public Optional<SseEmitter> findById(long customerId) {

		return Optional.ofNullable(emitters.get(customerId));
	}

	public SseEmitter save(long customerId, SseEmitter sseEmitter) {

		// 연결종료 시
		sseEmitter.onCompletion(() -> {
			log.debug("콜백함수 onCompletion called");
			log.debug("SSE 연결 종료: 사용자 Id: {}", customerId);
			deleteById(customerId);
		});

		//유효기간 만료시
		sseEmitter.onTimeout(() -> {
			log.debug("콜백함수 onTimeout called");
			log.debug("SSE 연결 시간 만료: 사용자 Id: {}", customerId);
			deleteById(customerId);
		});

		//클라이언트와 연결이 끊어졌을 때 동작
		sseEmitter.onError(e -> {
			log.warn("콜백함수 onError called");
			log.warn("SSE 연결 오류: 사용자 Id: {}, 원인: {}", customerId, e.getMessage());
			deleteById(customerId);
		});

		emitters.put(customerId, sseEmitter);
		return emitters.get(customerId);
	}

	// 안전하게 SseEmitter 객체 삭제
	public void deleteById(long customerId) {
		SseEmitter emitter = emitters.get(customerId);

		if (emitter != null) {
			log.info("sseEmitter 객체 제거 : 사용자 Id {}", customerId);
			emitters.remove(customerId);
		}
	}

}