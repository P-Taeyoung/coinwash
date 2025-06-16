package pp.coinwash.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	/* SERVICE_UNAVAILABLE */
	SSE_CONNECTION_FAILED(503, "SSE 연결 오류가 발생하였습니다."),

	/* 500 INTERNAL_SERVER_ERROR */
	INTERNAL_SERVER_ERROR(500, "서버 오류가 발생했습니다.")
	;

	private final Integer httpStatus;
	private final String message;
}
