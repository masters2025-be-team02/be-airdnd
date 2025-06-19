package kr.kro.airbob.domain.accommodation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.kro.airbob.domain.accommodation.entity.Accommodation;
import kr.kro.airbob.domain.accommodation.entity.Address;
import kr.kro.airbob.domain.accommodation.repository.querydsl.AccommodationRepositoryCustom;
import kr.kro.airbob.domain.image.AccommodationImage;
import kr.kro.airbob.domain.recentlyViewed.projection.RecentlyViewedProjection;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long>, AccommodationRepositoryCustom {
    Optional<Address> findAddressById(Long accommodationId);

    @Query("select a.member.id from Accommodation a where a.id = :id")
    Optional<Long> findHostIdByAccommodationId(Long id);

	@Query("""
	SELECT 
		new kr.kro.airbob.domain.recentlyViewed.projection.RecentlyViewedProjection(a.id, a.name, a.thumbnailUrl, AVG(r.rating))
	FROM Accommodation a
	LEFT JOIN Review r ON a.id = r.accommodation.id
	WHERE a.id IN :accommodationIds
	GROUP BY a.id
	""")
	List<RecentlyViewedProjection> findRecentlyViewedProjectionByIds(
		@Param("accommodationIds") List<Long> accommodationIds);

	@Query("""
	SELECT 
		ai
	FROM AccommodationImage ai
	WHERE ai.accommodation.id IN :accommodationIds
	ORDER BY ai.accommodation.id
	""")
	List<AccommodationImage> findAccommodationImagesByAccommodationIds(
		@Param("accommodationIds") List<Long> accommodationIds);
}
