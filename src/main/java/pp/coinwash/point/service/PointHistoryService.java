package pp.coinwash.point.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pp.coinwash.point.domain.dto.PointHistoryRequestDto;
import pp.coinwash.point.domain.entity.PointHistory;
import pp.coinwash.point.domain.repository.PointHistoryRepository;
import pp.coinwash.user.domain.entity.Customer;
import pp.coinwash.user.domain.repository.CustomerRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointHistoryService {

	private final PointHistoryRepository pointHistoryRepository;
	private final CustomerRepository customerRepository;

	@Transactional
	public void usePoint(PointHistoryRequestDto dto) {
		// try {
			Customer customer = customerRepository.findValidateCustomerToUsePoints(dto.customerId())
				.orElseThrow(() -> new RuntimeException("해당하는 사용자를 찾을 수 없습니다."));

			if (customer.getPoints() < dto.changedPoint()) {
				throw new RuntimeException("포인트가 부족합니다.");
			}

			customer.usePoints(dto.changedPoint());

			pointHistoryRepository.save(PointHistory.of(dto));
	}

	@Transactional
	public void earnPoint(PointHistoryRequestDto dto) {

			Customer customer = customerRepository.findValidateCustomerToUsePoints(dto.customerId())
				.orElseThrow(() -> new RuntimeException("해당하는 사용자를 찾을 수 없습니다."));

			customer.earnPoints(dto.changedPoint());

			pointHistoryRepository.save(PointHistory.of(dto));
	}

	public Page<PointHistory> getPointHistory(long customerId, Pageable pageable) {

		return pointHistoryRepository.findAllByCustomerId(customerId, pageable);
	}
}
