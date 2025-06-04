package pp.coinwash.address.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pp.coinwash.address.dto.AddressDto;
import pp.coinwash.address.dto.KakaoAddressResponse;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoAddressService {

	private final int pageSize = 5;
	private final WebClient webClient;

	//비동기 방식
	public Mono<List<AddressDto>> searchAddress(String query, Integer page) {
		return getAddressFromKakao(query, page)
			.map(KakaoAddressResponse::getDocuments)
			.map(documents -> documents.stream()
				.map(AddressDto::from)
				.collect(Collectors.toList()))
			.timeout(Duration.ofSeconds(10))                    // 타임아웃 설정
			.onErrorReturn(Collections.emptyList())             // 에러시 빈 리스트 반환
			.doOnSuccess(result -> log.info("주소 검색 완료: {}건", result.size()));
	}


	private Mono<KakaoAddressResponse> getAddressFromKakao(String query, Integer page) {
		log.info("주소 검색: query={}, page={}, size={}", query, page, pageSize);

		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/keyword.json")
				.queryParam("query", query)
				.queryParamIfPresent("page", java.util.Optional.ofNullable(page))
				.queryParamIfPresent("size", java.util.Optional.of(pageSize))
				.build())
			.retrieve()
			.bodyToMono(KakaoAddressResponse.class)
			.doOnSuccess(response -> log.info("주소 검색 성공: 총 {}건 {}", response.getMeta().getTotalCount(), response.getDocuments()))
			.doOnError(error -> log.error("주소 검색 실패: {}", error.getMessage()));
	}
}
