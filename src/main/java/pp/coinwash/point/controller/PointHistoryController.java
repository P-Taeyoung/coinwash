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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pp.coinwash.common.dto.PagedResponseDto;
import pp.coinwash.point.application.PointHistoryApplication;
import pp.coinwash.point.domain.dto.PointHistoryRequestDto;
import pp.coinwash.point.domain.dto.PointHistoryResponseDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/point")
@Tag(name = "포인트 관리", description = "포인트 관리 API")
public class PointHistoryController {

	private final PointHistoryApplication pointHistoryApplication;

	@Operation(
		summary = "포인트 내역 조회",
		tags = {"포인트 관리"},
		description = "고객이 자신의 포인트 내역을 조회. 사용/충전된 포인트 내역을 확인."
	)
	@GetMapping
	public ResponseEntity<PagedResponseDto<PointHistoryResponseDto>> getPointHistory(
		@RequestParam long customerId,
		@PageableDefault(sort = "pointHistoryId", direction = Sort.Direction.DESC)
		Pageable pageable) {

		return ResponseEntity.ok(pointHistoryApplication.getPointHistory(customerId, pageable));
	}

	@Operation(
		summary = "포인트 충전",
		tags = {"포인트 관리"},
		description = "고객 포인트 충전. 동시에 포인트 데이터에 접근하는 경우 가장 첫번째로 접근한 요청만 성공."
	)
	@PatchMapping
	public ResponseEntity<String> earnPoint(@RequestParam long customerId, @RequestParam int points) {

		pointHistoryApplication.earnPoints(PointHistoryRequestDto.earnPoint(customerId, points));
		return ResponseEntity.ok(points + "포인트가 적립되었습니다.");
	}

}
