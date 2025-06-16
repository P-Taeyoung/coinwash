package pp.coinwash.notification.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pp.coinwash.notification.domain.dto.NotificationRequestDto;
import pp.coinwash.notification.domain.dto.repository.SseEmitterRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseEmitterService {
	//SSE 이벤트 타임아웃
	private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

	private final SseEmitterRepository emitterRepository;

	public SseEmitter subscribe(Long customerId) {

		//기존 이미 연결이 되어있다면 끊고 다시 연결
		emitterRepository.deleteById(customerId);

		SseEmitter sseEmitter = emitterRepository.save(customerId, new SseEmitter(DEFAULT_TIMEOUT));

		// 첫 구독 시 이벤트 발생
		// 발생시키지 않고 하나의 데이터도 전송되지 않는다면 유효시간이 만료되고 503 에러 발생하기 때문
		sendConnectEvent(customerId, sseEmitter);

		return sseEmitter;
	}

	public void sendToCustomer(Long customerId, NotificationRequestDto dto) {

		SseEmitter sseEmitter = emitterRepository.findById(customerId).orElse(null);

		if (sseEmitter == null) {
			log.info("알림 대상자 SSE 연결 비활성: 대상자 Id : {}", customerId);
			return;
		}

		try {
			sseEmitter.send(
				SseEmitter.event()
					.name("notification") //이벤트 타입을 지정 클라이언트 측에서 해당 이벤트 타입에 따라 처리 가능
					.data(dto)
			);
		} catch (IOException e) {
			log.error("SSE 알림 전송 실패: 대상자 Id: {}, 원인: {}", customerId, e.getMessage());
			throw new RuntimeException("SSE 알림 전송 실패");
		}

	}

	public void unsubscribe(Long userId) {
		log.info("SSE 연결 종료 : 대상자 Id: {}", userId);
		emitterRepository.deleteById(userId);
	}

	@Scheduled(fixedRate = 60 * 1000)
	public void sendHeartbeats() {
		emitterRepository.getEmitters().forEach(( customerId, emitter) -> {
			try {
				emitter.send(
					SseEmitter.event()
						.name("heartbeat")
						.data(""));
				log.debug("하트비트 전송 성공: 사용자 Id: {}", customerId);
			} catch (IOException e) {
				log.error("하트비트 전송 실패: 사용자 Id: {}, 원인: {}", customerId, e.getMessage());
				throw new RuntimeException("SSE 연결 실패");
			}
		});
	}


	private void sendConnectEvent(Long customerId, SseEmitter emitter) {
		try {
			emitter.send(
				SseEmitter.event()
					.name("connect")
					.data(Map.of(
						"message", "SSE 연결 성공",
						"customerId", customerId,
						"timestamp", LocalDateTime.now().toString()
					))
			);
			log.info("SSE 연결 성공: 사용자 Id: {}", customerId);
		} catch (IOException e) {
			log.error("SSE 연결 실패: 사용자 Id: {}, 원인: {}", customerId, e.getMessage());
			throw new RuntimeException("SSE 연결 실패");
		}
	}
}
