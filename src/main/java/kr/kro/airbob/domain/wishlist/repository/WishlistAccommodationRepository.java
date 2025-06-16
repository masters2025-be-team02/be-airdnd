package kr.kro.airbob.domain.wishlist.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kr.kro.airbob.domain.wishlist.WishlistAccommodation;
import kr.kro.airbob.domain.wishlist.dto.projection.WishlistAmenityProjection;
import kr.kro.airbob.domain.wishlist.dto.projection.WishlistImageProjection;
import kr.kro.airbob.domain.wishlist.dto.projection.WishlistRatingProjection;

@Repository
public interface WishlistAccommodationRepository extends JpaRepository<WishlistAccommodation, Long> {

	void deleteAllByWishlistId(Long wishlistId);

	@Query("""
		SELECT 
			wa.wishlist.id,
			COUNT(wa)
		FROM WishlistAccommodation  wa
		WHERE wa.wishlist.id IN :wishlistIds
		GROUP BY wa.wishlist.id
""")
	Map<Long, Long> countByWishlistIds(@Param("wishlistIds") List<Long> wishlistIds);

	@Query(value = """
		SELECT 
			wishlist_id,
			thumbnail_url
		FROM (
			SELECT 
				wa.wishlist_id,
				a.thumbnail_url,
				ROW_NUMBER() OVER (
					PARTITION BY wa.wishlist_id ORDER BY wa.created_at DESC
				) as rn
			FROM wishlist_accommodation wa
			JOIN accommodation a ON wa.accommodation_id = a.id
			WHERE wa.wishlist_id IN :wishlistIds
		) ranked
		WHERE rn = 1
""", nativeQuery = true)
	Map<Long, String> findLatestThumbnailUrlsByWishlistIds(@Param("wishlistIds") List<Long> wishlistIds);

	@Query("""
		SELECT wa 
		FROM WishlistAccommodation wa
		JOIN FETCH wa.accommodation a
		WHERE wa.wishlist.id = :wishlistId
	 	AND (:lastCreatedAt IS NULL
	 		OR wa.createdAt < :lastCreatedAt
	 		OR (wa.createdAt = :lastCreatedAt AND wa.id < :lastId))
	 	ORDER BY wa.createdAt DESC, wa.id DESC 
""")
	Slice<WishlistAccommodation> findByWishlistIdWithCursor(
		@Param("wishlistId") Long wishlistId,
		@Param("lastId") Long lastId,
		@Param("lastCreatedAt") LocalDateTime lastCreatedAt,
		Pageable pageable);

	@Query("""
        SELECT 
        	new kr.kro.airbob.domain.wishlist.dto.projection.WishlistImageProjection(wa.id, ai.image_url)
        FROM WishlistAccommodation wa
        JOIN AccommodationImage ai ON wa.accommodation.id = ai.accommodation.id
        WHERE wa.id IN :wishlistAccommodationIds
        ORDER BY wa.id, ai.id
        """)
	List<WishlistImageProjection> findAccommodationImagesByWishlistAccommodationIds(@Param("wishlistAccommodationIds") List<Long> wishlistAccommodationIds);

	@Query(value = """
		SELECT 
			 new kr.kro.airbob.domain.wishlist.dto.projection.WishlistAmenityProjection(wa.id, aa.amenity.name, aa.count)
		FROM WishlistAccommodation wa
		JOIN AccommodationAmenity aa ON wa.accommodation.id = aa.accommodation.id
		WHERE wa.id IN :wishlistAccommodationIds
		ORDER BY wa.id, aa.amenity.name
		""")
	List<WishlistAmenityProjection> findAccommodationAmenitiesByWishlistAccommodationIds(
		@Param("wishlistAccommodationIds") List<Long> wishlistAccommodationIds);

	@Query(value = """
		SELECT 
			new kr.kro.airbob.domain.wishlist.dto.projection.WishlistRatingProjection(wa.id, AVG(r.rating))
		FROM WishlistAccommodation  wa
		LEFT JOIN Review r ON wa.accommodation.id = r.accommodation.id
		WHERE wa.id IN :wishlistAccommodationIds
		GROUP BY wa.id
		""")
	List<WishlistRatingProjection> findAccommodationRatingsByWishlistAccommodationIds(
		@Param("wishlistAccommodationIds")	List<Long> wishlistAccommodationIds);

	boolean existsByWishlistIdAndAccommodationId(Long wishlistId, Long accommodationId);
}
