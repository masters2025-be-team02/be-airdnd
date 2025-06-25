package kr.kro.airbob.domain.wishlist.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kr.kro.airbob.domain.wishlist.WishlistAccommodation;

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

	boolean existsByWishlistIdAndAccommodationId(Long wishlistId, Long accommodationId);

	@Query("select wa.wishlist.id from WishlistAccommodation wa where wa.id = :wishlistAccommodationId")
	Optional<Long> findWishlistIdByWishlistAccommodationId(
		@Param("accommodationId") Long wishlistAccommodationId);

	@Query("""
	SELECT 
		wa.accommodation.id
	FROM WishlistAccommodation wa 
	WHERE wa.wishlist.member.id = :memberId
	AND wa.accommodation.id IN :accommodationIds
	""")
	Set<Long> findAccommodationIdsByMemberIdAndAccommodationIds(
		@Param("memberId") Long memberId,
		@Param("accommodationIds") List<Long> accommodationIds);
}

