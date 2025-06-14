package kr.kro.airbob.domain.wishlist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kr.kro.airbob.domain.wishlist.WishlistAccommodation;

@Repository
public interface WishlistAccommodationRepository extends JpaRepository<WishlistAccommodation, Long> {

	void deleteAllByWishlistId(Long wishlistId);
}
