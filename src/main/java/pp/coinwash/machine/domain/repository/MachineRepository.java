package pp.coinwash.machine.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pp.coinwash.machine.domain.entity.Machine;

public interface MachineRepository extends JpaRepository<Machine, Long> {
}
