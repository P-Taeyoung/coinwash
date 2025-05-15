package pp.coinwash.user.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pp.coinwash.user.domain.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
	Boolean existsByLoginIdAndDeletedAtIsNull(String id);
	Optional<Customer> findByCustomerIdAndDeletedAtIsNull(Long id);


}
