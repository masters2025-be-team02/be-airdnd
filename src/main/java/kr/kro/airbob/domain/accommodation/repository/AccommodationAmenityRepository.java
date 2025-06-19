package kr.kro.airbob.domain.accommodation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.kro.airbob.domain.accommodation.entity.AccommodationAmenity;
import kr.kro.airbob.domain.recentlyViewed.projection.RecentlyViewedAmenityProjection;

public interface AccommodationAmenityRepository extends JpaRepository<AccommodationAmenity, Long> {
    void deleteAllByAccommodationId(Long accommodationId);

    List<AccommodationAmenity> findAllByAccommodationId(Long accommodationId);

    void deleteByAccommodationId(Long accommodationId);

    boolean existsByAccommodationId(Long accommodationId);

	@Query("""
	SELECT 
		new kr.kro.airbob.domain.recentlyViewed.projection.RecentlyViewedAmenityProjection(a.id, aa.amenity.name, aa.count)
	FROM Accommodation a 
	JOIN AccommodationAmenity aa ON aa.accommodation.id = a.id
	WHERE a.id IN :accommodationIds
	ORDER BY a.id, aa.amenity.name
""")
	List<RecentlyViewedAmenityProjection> findRecentlyViewedAmenityProjectionByAccommodationIds(
		@Param("accommodationIds") List<Long> accommodationIds);

	@Query("""
    SELECT 
    	aa
    FROM AccommodationAmenity aa
    WHERE aa.accommodation.id IN :accommodationIds
    ORDER BY aa.accommodation.id, aa.amenity.name
    """)
	List<AccommodationAmenity> findAccommodationAmenitiesByAccommodationIds(
		@Param("accommodationIds") List<Long> accommodationIds);
}
