package pp.coinwash.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	/* 400 BAD_REQUEST */
	INSUFFICIENT_POINTS(400, "포인트가 부족합니다."),
	WRONG_TOKEN_PREFIX(400,  "토큰 접두사가 올바르지 않습니다."),
	INVALID_CREDENTIALS(401,  "아이디 또는 비밀번호가 올바르지 않습니다."),

	/* 403 FORBIDDEN */
	USER_NOT_RESERVED(403, "예약자가 아닙니다."),

	/* 404 NOT_FOUND */
	USER_NOT_FOUND(404, "해당하는 사용자를 찾을 수 없습니다."),
	NO_AVAILABLE_MACHINE(404,  "이용 가능한 기계를 찾을 수 없습니다."),
	LAUNDRY_NOT_FOUND(404, "해당하는 세탁소 정보를 찾을 수 없습니다."),
	RESERVED_MACHINE_NOT_FOUND(404, "예약된 기계를 찾을 수 없습니다."),
	NO_AUTHORIZED_MACHINE_FOUND(404, "권한을 지닌 기계 정보를 찾을 수 없습니다."),
	NO_AUTHORIZED_LAUNDRY_FOUND(404, "권한을 지닌 가게 정보를 찾을 수 없습니다."),

	/* 409 CONFLICT */
	ALREADY_USING_MACHINE(409, "기계가 이미 사용 중입니다."),
	LAUNDRY_ALREADY_EXISTS_NEARBY(409, "근처에 이미 세탁소가 존재합니다."),
	CONCURRENTLY_CHANGED_POINTS(409,  "동시에 포인트 변경 시도가 이루어졌습니다. 잠시 후에 다시 시도해주세요."),
	ALREADY_EXISTS_ID(409,  "이미 존재하는 아이디입니다."),


	/* SERVICE_UNAVAILABLE */
	SSE_CONNECTION_FAILED(503, "SSE 연결 오류가 발생하였습니다."),

	/* 500 INTERNAL_SERVER_ERROR */
	INTERNAL_SERVER_ERROR(500, "서버 오류가 발생했습니다."),
	FAILED_TO_SAVE_MACHINES(500, "기계 정보 저장에 실패했습니다."),
	FAILED_TO_UPDATE_MACHINES(500, "기계 정보 업데이트에 실패했습니다."),
	FAILED_TO_DELETE_MACHINES(500, "기계 정보 삭제에 실패했습니다."),
	FAILED_TO_CHANGE_MACHINE_STATUS(500, "기계 상태 변경에 실패했습니다."),

	/* SSE_ERROR */
	FAILED_TO_SEND_SSE_MESSAGE(500,  "SSE 메시지 전송에 실패했습니다."),
	FAILED_TO_CONNECT_SSE(500,  "SSE 연결에 실패했습니다."),
	SSE_CONNECTION_LOST(500, "SSE 연결이 예기치 않게 끊어졌습니다."),





	/* 503 SERVICE_UNAVAILABLE*/
	MACHINE_UNUSABLE(503, "현재 사용할 수 없는 기계입니다.")

	;

	private final Integer httpStatus;
	private final String message;
}
