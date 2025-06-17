package pp.coinwash.address.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pp.coinwash.address.dto.AddressDto;
import pp.coinwash.address.service.KakaoAddressService;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/address")
@Tag(name = "주소 검색", description = "카카오 맵 주소 검색 API")
public class KakaoAddressController {

	private final KakaoAddressService kakaoAddressService;

	@Operation(
		summary = "카카오 맵 주소 검색",
		tags = {"카카오 맵 주소 검색"},
		description = "카카오 맵 API 를 통해 주소 검색, 위도, 경도 정보와 주소 이름 반환"
	)
	@GetMapping
	public Mono<ResponseEntity<List<AddressDto>>> searchAddress(
		@RequestParam String query,
		@RequestParam Integer page) {

		return kakaoAddressService.searchAddress(query, page)
			.map(addressList -> addressList.isEmpty()
				? ResponseEntity.notFound().build()
				: ResponseEntity.ok(addressList));
	}

}
