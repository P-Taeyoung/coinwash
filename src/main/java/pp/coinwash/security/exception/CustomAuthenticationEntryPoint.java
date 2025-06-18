package pp.coinwash.security.exception;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import pp.coinwash.common.dto.ExceptionResponseDto;
import pp.coinwash.common.exception.ErrorCode;

@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws IOException, ServletException {

		log.error("사용자 인증 실패 : {}, URI: {}", authException.getMessage(), request.getRequestURI());

		ErrorCode errorCode = determineErrorCode(authException);

		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(errorCode.getHttpStatus());

		ObjectMapper objectMapper = new ObjectMapper();

		response.getWriter().write(objectMapper.writeValueAsString(
			ExceptionResponseDto.of(
				errorCode.getHttpStatus(),
				errorCode.getMessage(),
				null)));
	}

	private ErrorCode determineErrorCode(AuthenticationException authException) {
		if (authException instanceof BadCredentialsException) {
			return ErrorCode.INVALID_TOKEN;
		} else if (authException instanceof CredentialsExpiredException) {
			return ErrorCode.TOKEN_EXPIRED;
		} else if (authException instanceof InternalAuthenticationServiceException) {
			return ErrorCode.INTERNAL_AUTHENTICATION_ERROR;
		} else {
			return ErrorCode.AUTHENTICATION_FAILED;
		}
	}
}
