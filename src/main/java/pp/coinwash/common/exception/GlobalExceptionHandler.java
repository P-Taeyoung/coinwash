package pp.coinwash.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import pp.coinwash.common.dto.ExceptionResponseDto;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	/**
	 * CustomException 처리 - ErrorCode에 정의된 예외 반환
	 */
	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ExceptionResponseDto<Void>> handleCustomException(CustomException e) {
		log.error("CustomException 발생: {}", e.getMessage());
		return ResponseEntity.status(e.getErrorCode().getHttpStatus())
			.body(ExceptionResponseDto.of(e.getErrorCode().getHttpStatus(), e.getErrorCode().getMessage()));
	}


	/**
	 * RuntimeException 처리 - 서버 내부 오류 반환
	 */
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ExceptionResponseDto<Void>> handleRuntimeException(RuntimeException e) {
		log.error("RuntimeException 발생: ", e);
		return ResponseEntity.internalServerError()
			.body(ExceptionResponseDto.of(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus(),
				ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
	}
}
