package pp.coinwash.history.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import pp.coinwash.history.domain.entity.History;

public interface HistoryRepository extends JpaRepository<History, Long> {
	Page<History> findAllByCustomerId(long customerId, Pageable pageable);
}
