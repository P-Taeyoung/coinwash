package pp.coinwash.laundry.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pp.coinwash.laundry.domain.entity.Laundry;

public interface LaundryRepository extends JpaRepository<Laundry, Long> {
	List<Laundry> findByOwnerIdAndDeletedAtIsNull(long ownerId);
	Optional<Laundry> findByLaundryIdAndOwnerIdAndDeletedAtIsNull(long laundryId, long ownerId);



	@Query(value = "SELECT COUNT(l) > 0 FROM Laundry l " +
		"WHERE ST_Distance_Sphere(l.location, " +
		"ST_PointFromText(CONCAT('POINT(', :latitude, ' ', :longitude, ')'), 4326)) <= :distance AND l.deletedAt IS NULL")
	Boolean existsWithinDistance(
		@Param("longitude") double longitude,
		@Param("latitude") double latitude,
		@Param("distance") double distanceInMeters);

	@Query(value = "SELECT l FROM Laundry l " +
		"WHERE ST_Distance_Sphere(l.location, " +
		"ST_PointFromText(CONCAT('POINT(', :latitude, ' ', :longitude, ')'), 4326)) <= :distance AND l.deletedAt IS NULL")
	List<Laundry> findLaundriesNearBy(
		@Param("longitude") double longitude,
		@Param("latitude") double latitude,
		@Param("distance") double distanceInMeters
	);
}
