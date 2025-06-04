package pp.coinwash.user.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import pp.coinwash.user.domain.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
	Boolean existsByLoginIdAndDeletedAtIsNull(String id);
	Optional<Customer> findByCustomerIdAndDeletedAtIsNull(Long id);
	Optional<Customer> findByLoginIdAndDeletedAtIsNull(String loginId);

	@Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
	@Query("SELECT c FROM Customer c WHERE c.customerId = :customerId AND c.deletedAt IS NULL")
	Optional<Customer> findValidateCustomerToUsePoints(@Param("customerId") Long customerId);



}
