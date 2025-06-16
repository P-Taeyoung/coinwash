package pp.coinwash.laundry.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pp.coinwash.laundry.domain.dto.LaundryResponseDto;
import pp.coinwash.laundry.service.SearchLaundryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/laundries")
public class SearchLaundryController {

	private final SearchLaundryService searchLaundryService;

	@GetMapping
	public ResponseEntity<List<LaundryResponseDto>> findLaundriesNearBy(
		@RequestParam double longitude,
		@RequestParam double latitude,
		@RequestParam double distance) {

		return ResponseEntity.ok(searchLaundryService.findLaundriesNearBy(longitude, latitude, distance));
	}
}
