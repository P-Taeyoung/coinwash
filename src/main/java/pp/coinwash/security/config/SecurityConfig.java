package pp.coinwash.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import pp.coinwash.security.exception.CustomAccessDeniedException;
import pp.coinwash.security.exception.CustomAuthenticationEntryPoint;
import pp.coinwash.security.filter.JwtFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtFilter jwtFilter;

	private final CustomAccessDeniedException customAccessDeniedException;
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(exception -> {
				exception.authenticationEntryPoint(customAuthenticationEntryPoint);
				exception.accessDeniedHandler(customAccessDeniedException);
			});

		http
			.authorizeHttpRequests(authorizeRequests ->
				authorizeRequests
					//TODO 허용할 경로 추가
					.requestMatchers("/**").permitAll()
					.requestMatchers("")
					.permitAll()
					.anyRequest().authenticated());

		http.addFilterBefore(jwtFilter, AuthorizationFilter.class);

		return http.build();
	}

}
