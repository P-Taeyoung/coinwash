package pp.coinwash.user.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pp.coinwash.user.domain.entity.Owner;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
}
