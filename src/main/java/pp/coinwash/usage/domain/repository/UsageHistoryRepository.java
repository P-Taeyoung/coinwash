package pp.coinwash.usage.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import pp.coinwash.usage.domain.entity.UsageHistory;

public interface UsageHistoryRepository extends JpaRepository<UsageHistory, Long> {
	Page<UsageHistory> findAllByCustomerId(long customerId, Pageable pageable);
}
