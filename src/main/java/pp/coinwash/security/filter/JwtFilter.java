package pp.coinwash.security.filter;

import java.io.IOException;
import java.util.stream.Collectors;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pp.coinwash.security.JwtProvider;
import pp.coinwash.security.dto.CustomUserDetails;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private final JwtProvider jwtProvider;

	public static final String TOKEN_HEADER = "Authorization";
	public static final String TOKEN_PREFIX = "Bearer ";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String requestURI = request.getRequestURI();
		log.info("requestURI: {}", requestURI);

		if (requestURI.startsWith("/swagger-ui/")
			|| requestURI.startsWith("/v3/api-docs")
			|| requestURI.startsWith("/login")
		) {
			filterChain.doFilter(request, response);
			return;
		}

		try {

			String token = tokenFromHeader(request);

			if (token == null) {
				filterChain.doFilter(request, response);
				return;
			}

			SecurityContextHolder.getContext().setAuthentication(jwtProvider.getAuthentication(token));

			CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();
			log.info("사용자 정보: {}, {}, {}", userDetails.getUserId(), userDetails.getUsername(),
				userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));

		} catch (AuthenticationException e) {

			log.error("JWT 인증 실패: {}", e.getMessage());
			throw e;
		} catch (Exception e) {

			log.error("JWT 처리 중 예상치 못한 오류: {}", e.getMessage());
			throw new InternalAuthenticationServiceException("JWT 처리 중 오류 발생", e);
		}

		filterChain.doFilter(request, response);
	}

	private String tokenFromHeader(HttpServletRequest request) {
		String token = request.getHeader(TOKEN_HEADER);
		log.info("Token : {}", token);

		if (!StringUtils.hasText(token)) {
			return null;
		}

		// 토큰 형식이 잘못된 경우 예외 발생
		if (!token.startsWith(TOKEN_PREFIX)) {
			throw new RuntimeException("잘못된 토큰 형식입니다.");
		}

		return token.substring(TOKEN_PREFIX.length()).trim();
	}
}
