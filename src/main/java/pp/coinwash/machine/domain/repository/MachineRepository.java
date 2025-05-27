package pp.coinwash.machine.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pp.coinwash.machine.domain.entity.Machine;

public interface MachineRepository extends JpaRepository<Machine, Long> {
	List<Machine> findByLaundryLaundryIdAndDeletedAtIsNull(long laundryId);
	Optional<Machine> findByMachineIdAndLaundryOwnerId(long machineId, long ownerId);
}
