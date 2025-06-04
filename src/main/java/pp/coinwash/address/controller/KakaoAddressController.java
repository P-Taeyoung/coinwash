package pp.coinwash.address.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pp.coinwash.address.dto.AddressDto;
import pp.coinwash.address.service.KakaoAddressService;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/address")
public class KakaoAddressController {

	private final KakaoAddressService kakaoAddressService;

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
