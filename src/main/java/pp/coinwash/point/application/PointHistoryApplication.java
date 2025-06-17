package pp.coinwash.point.application;

import static pp.coinwash.common.exception.ErrorCode.*;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pp.coinwash.common.dto.PagedResponseDto;
import pp.coinwash.common.exception.CustomException;
import pp.coinwash.common.exception.ErrorCode;
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
			throw new CustomException(CONCURRENTLY_CHANGED_POINTS);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new CustomException(INTERNAL_SERVER_ERROR);
		}
	}

	public void earnPoints(PointHistoryRequestDto dto) {
		try {
			pointHistoryService.earnPoint(dto);

		} catch (OptimisticLockingFailureException e) {
			throw new CustomException(CONCURRENTLY_CHANGED_POINTS);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new CustomException(INTERNAL_SERVER_ERROR);
		}
	}

	public PagedResponseDto<PointHistoryResponseDto> getPointHistory(long customerId, Pageable pageable) {

		return PagedResponseDto.from(
			pointHistoryService.getPointHistory(customerId, pageable)
				.map(PointHistoryResponseDto::from));
	}
}
