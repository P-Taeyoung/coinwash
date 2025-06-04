package pp.coinwash.point.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pp.coinwash.common.dto.PagedResponseDto;
import pp.coinwash.point.application.PointHistoryApplication;
import pp.coinwash.point.domain.dto.PointHistoryRequestDto;
import pp.coinwash.point.domain.dto.PointHistoryResponseDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/point")
public class PointHistoryController {

	private final PointHistoryApplication pointHistoryApplication;

	@GetMapping
	public ResponseEntity<PagedResponseDto<PointHistoryResponseDto>> getPointHistory(
		@RequestParam long customerId,
		@PageableDefault(sort = "pointHistoryId", direction = Sort.Direction.DESC)
		Pageable pageable) {

		return ResponseEntity.ok(pointHistoryApplication.getPointHistory(customerId, pageable));
	}

	@PatchMapping
	public ResponseEntity<String> earnPoint(@RequestParam long customerId, @RequestParam int points) {

		pointHistoryApplication.earnPoints(PointHistoryRequestDto.earnPoint(customerId, points));
		return ResponseEntity.ok(points + "포인트가 적립되었습니다.");
	}

}
