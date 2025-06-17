package pp.coinwash.laundry.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pp.coinwash.laundry.domain.dto.LaundryResponseDto;
import pp.coinwash.laundry.service.SearchLaundryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/laundries")
@Tag(name = "주변 세탁소 조회", description = "주변 세탁소 조회 API")
public class SearchLaundryController {

	private final SearchLaundryService searchLaundryService;

	@Operation(
		summary = "주변 세탁소 조회",
		tags = {"주변 세탁소 조회"},
		description = "사용자가 설정한 위치로부터 설정 거리 내 등록된 세탁소를 조회, 위도 경도를 통해 계산"
	)
	@GetMapping
	public ResponseEntity<List<LaundryResponseDto>> findLaundriesNearBy(
		@RequestParam double longitude,
		@RequestParam double latitude,
		@RequestParam double distance) {

		return ResponseEntity.ok(searchLaundryService.findLaundriesNearBy(longitude, latitude, distance));
	}
}
