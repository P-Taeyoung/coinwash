package pp.coinwash.user.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pp.coinwash.user.domain.entity.Owner;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
	Boolean existsByLoginIdAndDeletedAtIsNull(String id);
	Optional<Owner> findByOwnerIdAndDeletedAtIsNull(Long id);

}
