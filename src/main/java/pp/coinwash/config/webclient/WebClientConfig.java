package pp.coinwash.config.webclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

	@Value("${kakao.api.key}")
	private String kakaoApiKey;

	@Bean
	public WebClient webClient() {
		return WebClient.builder()
			.baseUrl("https://dapi.kakao.com/v2/local/search")
			.defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoApiKey)
			.build();
	}
}
