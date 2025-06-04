package pp.coinwash.point.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import pp.coinwash.point.domain.entity.PointHistory;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
	Page<PointHistory> findAllByCustomerId(Long customerId, Pageable pageable);
}
