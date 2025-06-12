package pp.coinwash.machine.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import pp.coinwash.machine.domain.entity.Machine;
import pp.coinwash.machine.domain.type.MachineType;

public interface MachineRepository extends JpaRepository<Machine, Long> {
	List<Machine> findByLaundryLaundryIdAndDeletedAtIsNull(long laundryId);

	@Lock(LockModeType.PESSIMISTIC_READ)
	Optional<Machine> findByMachineIdAndLaundryOwnerId(long machineId, long ownerId);

	@Query("SELECT m FROM Machine m WHERE m.machineId = :machineId AND m.machineType = :machineTyp AND m.deletedAt IS NULL ")
	@Lock(LockModeType.PESSIMISTIC_READ)
	Optional<Machine> findUsableMachineWithLock(@Param("machineId") long machineId
		, @Param("machineTyp") MachineType machineTyp);

	@Query("SELECT m FROM Machine m WHERE m.machineId = :machineId AND (m.usageStatus = 'USABLE' OR (m.endTime IS NOT NULL AND m.endTime < :currentTime) AND m.deletedAt IS NULL)")
	@Lock(LockModeType.PESSIMISTIC_READ)
	Optional<Machine> findUsableMachineByMachineId(long machineId, LocalDateTime currentTime);

	@Query("SELECT m FROM Machine m WHERE m.machineId = :machineId AND m.customerId = :customerId AND m.deletedAt IS NULL")
	Optional<Machine> findReserveMachine(long machineId, long customerId);
}
