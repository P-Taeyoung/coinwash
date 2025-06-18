package pp.coinwash.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import pp.coinwash.security.dto.CustomUserDetails;
import pp.coinwash.security.dto.UserAuthDto;
import pp.coinwash.security.dto.UserRole;

@Component
@Slf4j
public class JwtProvider {

	private final SecretKey secretKey;

	//토큰 유효시간
	private static final long tokenValidTime = 1000L * 60 * 60 * 24;

	public JwtProvider(@Value("${spring.jwt.secret}") String secret) {
		secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
	}

	//토큰 생성
	public String generateToken(UserAuthDto userAuthDto) {

		return Jwts.builder()
			.claim("userId", userAuthDto.userId())
			.claim("name", userAuthDto.userName())
			.claim("role", userAuthDto.role().name())
			.issuedAt(new Date(System.currentTimeMillis()))
			.expiration(new Date(System.currentTimeMillis() + tokenValidTime))
			.signWith(secretKey)
			.compact();
	}


	public Authentication getAuthentication(String token) {
		CustomUserDetails userDetails = new CustomUserDetails(getUserFromToken(token));
		return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	}

	private UserAuthDto getUserFromToken(String token) {
		Claims claims = getClaimsFromToken(token);

		return UserAuthDto.builder()
			.userId(claims.get("userId", Long.class))
			.userName(claims.get("name", String.class))
			.role(UserRole.valueOf(claims.get("role", String.class)))
			.build();
	}

	private Claims getClaimsFromToken(String token) {
		try {
			log.info("Parsing token : {}", token);
			return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();

		} catch (ExpiredJwtException e) {
			log.error("Token expired: {}", e.getMessage());
			throw new CredentialsExpiredException("토큰이 만료되었습니다.", e);

		} catch (MalformedJwtException e) {
			log.error("Malformed JWT: {}", e.getMessage());
			throw new BadCredentialsException("잘못된 형식의 토큰입니다.", e);

		} catch (UnsupportedJwtException e) {
			log.error("Unsupported JWT: {}", e.getMessage());
			throw new BadCredentialsException("지원되지 않는 토큰입니다.", e);

		} catch (IllegalArgumentException e) {
			log.error("JWT claims string is empty: {}", e.getMessage());
			throw new BadCredentialsException("토큰 클레임이 비어있습니다.", e);

		} catch (io.jsonwebtoken.security.SignatureException e) {
			log.error("Invalid JWT signature: {}", e.getMessage());
			throw new BadCredentialsException("유효하지 않은 토큰 서명입니다.", e);

		} catch (Exception e) {
			log.error("Unknown error during token parsing: {}", e.getMessage());
			throw new InternalAuthenticationServiceException("토큰 파싱 중 알 수 없는 오류가 발생했습니다.", e);
		}
	}

}
