package kr.kro.airbob.domain.wishlist.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
	Map<Long, Long> countByWishlistIds(List<Long> wishlistIds);

	// projection 고려
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
	Map<Long, String> findLatestThumbnailUrlsByWishlistIds(List<Long> wishlistIds);
}
