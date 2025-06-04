package pp.coinwash.point.application;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pp.coinwash.common.dto.PagedResponseDto;
import pp.coinwash.point.domain.dto.PointHistoryRequestDto;
import pp.coinwash.point.domain.dto.PointHistoryResponseDto;
import pp.coinwash.point.service.PointHistoryService;
import pp.coinwash.user.domain.entity.Customer;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointHistoryApplication {

	private final PointHistoryService pointHistoryService;

	public void usePoints(PointHistoryRequestDto dto) {
		try {
			pointHistoryService.usePoint(dto);

		} catch (OptimisticLockingFailureException e) {
			throw new RuntimeException("동시에 포인트 변동이 이뤄졌습니다. 다시 시도해주세요.");

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;  // 원본 예외를 다시 던지거나 적절히 처리
		}
	}

	public void earnPoints(PointHistoryRequestDto dto) {
		try {
			pointHistoryService.earnPoint(dto);

		} catch (OptimisticLockingFailureException e) {
			throw new RuntimeException("동시에 포인트 변동이 이뤄졌습니다. 다시 시도해주세요.");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}

	public PagedResponseDto<PointHistoryResponseDto> getPointHistory(long customerId, Pageable pageable) {

		return PagedResponseDto.from(
			pointHistoryService.getPointHistory(customerId, pageable)
				.map(PointHistoryResponseDto::from));
	}
}
