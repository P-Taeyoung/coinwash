package pp.coinwash.machine.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.domain.type.MachineType;

public interface MachineRepository extends JpaRepository<Machine, Long> {
	List<Machine> findByLaundryLaundryIdAndDeletedAtIsNull(long laundryId);
	Optional<Machine> findByMachineIdAndLaundryOwnerId(long machineId, long ownerId);

	@Query("SELECT m FROM Machine m WHERE m.machineId = :machineId AND m.machineType = :machineTyp")
	Optional<Machine> findMachineByMachineId(@Param("machineId") long machineId
		, @Param("machineTyp") MachineType machineTyp);

	@Query("SELECT m FROM Machine m WHERE m.machineId = :machineId AND m.usageStatus = 'USABLE'")
	Optional<Machine> findUsableMachineByMachineId(long machineId);
}
