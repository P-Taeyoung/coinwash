package pp.coinwash.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.DecodingException;
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
			return e.getClaims();
		} catch (DecodingException e) {
			log.error("Decoding error: {}", e.getMessage());
			throw new RuntimeException("Invalid token format", e);
		} catch (Exception e) {
			log.error("Unknown error during token parsing: {}", e.getMessage());
			throw new RuntimeException("Unknown error during token parsing", e);
		}
	}

}
