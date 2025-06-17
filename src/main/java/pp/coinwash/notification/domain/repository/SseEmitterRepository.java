package pp.coinwash.notification.domain.repository;

import java.util.Map;
import java.util.Optional;
import java.util.Queue;
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

	public SseEmitter save(long customerId ,SseEmitter sseEmitter) {

		// 연결종료 시
		sseEmitter.onCompletion(() -> {
			log.info("SSE 연결 종료 : 사용자 Id {}", customerId);
			deleteById(customerId);
		});

		//유효기간 만료시
		sseEmitter.onTimeout(() -> {
			log.debug("SSE 연결 시간 만료: 사용자 Id: {}", customerId);
			deleteById(customerId);});

		//클라이언트와 연결이 끊어졌을 때 동작
		sseEmitter.onError(e -> {
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
			try {
				emitter.complete();
			} catch (Exception e) {
				log.warn("SSE 연결 종료 중 오류 발생 : 사용자 Id {}, 원인: {}", customerId, e.getMessage());
			} finally {
				emitters.remove(customerId);
			}
		}
	}


}