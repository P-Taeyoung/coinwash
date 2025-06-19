package pp.coinwash.security.filter;

import java.io.IOException;
import java.util.stream.Collectors;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
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

		if (isExcludedPath(requestURI)) {
			filterChain.doFilter(request, response);
			return;
		}

		try {

			String authHeader = request.getHeader(TOKEN_HEADER);

			if (authHeader != null) {
				Authentication authentication = jwtProvider.getAuthentication(authHeader);

				if (authentication != null) {
					SecurityContextHolder.getContext().setAuthentication(authentication);
					logUserInfo(authentication);
				}
			}

		} catch (AuthenticationException e) {

			log.error("JWT 인증 실패: {}", e.getMessage());
			throw e;
		} catch (Exception e) {

			log.error("JWT 처리 중 예상치 못한 오류: {}", e.getMessage());
			throw new InternalAuthenticationServiceException("JWT 처리 중 오류 발생", e);
		}

		filterChain.doFilter(request, response);
	}

	private boolean isExcludedPath(String requestURI) {
		return requestURI.startsWith("/swagger-ui/**")
			|| requestURI.startsWith("/v3/api-docs/**")
			|| requestURI.startsWith("/swagger-resources")
			|| requestURI.startsWith("/webjars/")
			|| requestURI.equals("/favicon.ico")
			|| requestURI.startsWith("/login")
			|| requestURI.startsWith("/error");
	}

	private void logUserInfo(Authentication authentication) {
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		log.info("사용자 정보: {}, {}, {}",
			userDetails.getUserId(),
			userDetails.getUsername(),
			userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.toList()));
	}
}
