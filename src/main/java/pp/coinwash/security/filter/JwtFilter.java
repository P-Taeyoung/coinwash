package pp.coinwash.security.filter;

import java.io.IOException;
import java.util.stream.Collectors;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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
	public static final String TOKEN_COOKIE = "token"; // 🔧 쿠키명 상수 추가

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
			logAllCookies(request);


			// 🔧 1. 먼저 헤더에서 토큰 확인 (기존 로직)
			String authHeader = request.getHeader(TOKEN_HEADER);

			// 🔧 2. 헤더에 토큰이 없으면 쿠키에서 확인
			if (authHeader == null) {
				log.info("헤더에 토큰 없어서 쿠키에서 탐색");
				authHeader = getTokenFromCookie(request);
			}

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

	// 🔧 모든 쿠키 로깅 메서드 추가
	private void logAllCookies(HttpServletRequest request) {
		// 1. Raw Cookie Header 확인
		String cookieHeader = request.getHeader("Cookie");
		log.info("🔍 [RAW] Cookie Header: {}", cookieHeader);

		// 2. 쿠키 배열 확인
		Cookie[] cookies = request.getCookies();

		if (cookies == null) {
			log.info("🍪 cookies 배열이 null입니다!");
			return;
		}

		log.info("🍪 총 {} 개의 쿠키가 있습니다:", cookies.length);
		for (int i = 0; i < cookies.length; i++) {
			Cookie cookie = cookies[i];
			log.info("🍪 쿠키[{}]: {} = {} (길이: {})",
				i, cookie.getName(), cookie.getValue(), cookie.getValue().length());
		}
	}


	// 🔧 쿠키에서 토큰 추출 메서드 추가
	private String getTokenFromCookie(HttpServletRequest request) {
		// 🔧 1. 먼저 Raw Header에서 직접 파싱 시도
		String cookieHeader = request.getHeader("Cookie");
		if (cookieHeader != null) {
			log.info("🔍 Raw Cookie Header에서 토큰 탐색: {}", cookieHeader);

			String[] cookiePairs = cookieHeader.split(";");
			for (String pair : cookiePairs) {
				String trimmed = pair.trim();
				if (trimmed.startsWith(TOKEN_COOKIE + "=")) {
					String tokenValue = trimmed.substring(TOKEN_COOKIE.length() + 1);
					log.info("🎯 Raw Header에서 토큰 발견: {}", tokenValue);
					return tokenValue;
				}
			}
		}

		// 🔧 2. 기존 방식도 시도 (백업)
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (TOKEN_COOKIE.equals(cookie.getName())) {
					log.info("🎯 일반 Cookie에서 토큰 발견: {}", cookie.getValue());
					return cookie.getValue();
				}
			}
		}

		log.warn("⚠️ 토큰을 찾을 수 없습니다");
		return null;
	}

	private boolean isExcludedPath(String requestURI) {
		return requestURI.startsWith("/swagger-ui/**")
			|| requestURI.startsWith("/v3/api-docs/**")
			|| requestURI.startsWith("/swagger-resources")
			|| requestURI.startsWith("/webjars/")
			|| requestURI.equals("/favicon.ico")
			|| requestURI.equals("/")
			|| requestURI.equals("/api/address")
			|| requestURI.startsWith("/login")
			|| requestURI.startsWith("/error")
			|| requestURI.startsWith("/css")
			|| requestURI.startsWith("/js");
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
