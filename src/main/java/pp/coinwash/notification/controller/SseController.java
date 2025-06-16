package pp.coinwash.notification.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import pp.coinwash.notification.service.SseEmitterService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {

	private final SseEmitterService sseEmitterService;


	@PostMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter sseSubscribe(@RequestParam long customerId) {

		return sseEmitterService.subscribe(customerId);
	}

	@PostMapping("/unsubscribe")
	public ResponseEntity<String> sseUnsubscribe(@RequestParam long customerId) {

		sseEmitterService.unsubscribe(customerId);

		return ResponseEntity.ok("sse 연결 중지!");
	}
}
