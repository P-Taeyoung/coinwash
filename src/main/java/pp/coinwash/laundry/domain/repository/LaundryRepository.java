package pp.coinwash.laundry.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pp.coinwash.laundry.domain.entity.Laundry;

public interface LaundryRepository extends JpaRepository<Laundry, Long> {
	Page<Laundry> findByOwnerIdAndDeletedAtIsNull(long ownerId, Pageable pageable);
	Optional<Laundry> findByLaundryIdAndOwnerIdAndDeletedAtIsNull(long laundryId, long ownerId);

	@Query(value = "SELECT EXISTS(SELECT 1 FROM laundry " +
		"WHERE ST_Distance_Sphere(location, " +
		"point(:longitude, :latitude)) <= :distance LIMIT 1)",
		nativeQuery = true)
	boolean existsWithinDistance(
		@Param("latitude") double latitude,
		@Param("longitude") double longitude,
		@Param("distance") double distanceInMeters);
}
