package pp.coinwash.notification.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pp.coinwash.notification.service.SseEmitterService;
import pp.coinwash.security.dto.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
@Tag(name = "SSE 관리", description = "SSE 관리 API")
public class SseController {

	private final SseEmitterService sseEmitterService;

	@Operation(
		summary = "SSE 연결",
		tags = {"SSE 관리"},
		description = "해당 API 요청을 통해 SSE 연결. 연결된 사용자는 실시간 알림을 받을 수 있음."
	)
	@PostMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter sseSubscribe(@AuthenticationPrincipal CustomUserDetails userDetails) {

		return sseEmitterService.subscribe(userDetails.getUserId());
	}

	@Operation(
		summary = "SSE 연결 해제",
		tags = {"SSE 관리"},
		description = "해당 API 요청을 통해 SSE 연결 해제. 정상적으로 SSE 연결을 해제"
	)
	@PostMapping("/unsubscribe")
	public ResponseEntity<String> sseUnsubscribe(@AuthenticationPrincipal CustomUserDetails userDetails) {

		sseEmitterService.unsubscribe(userDetails.getUserId());

		return ResponseEntity.ok("sse 연결 중지!");
	}
}
