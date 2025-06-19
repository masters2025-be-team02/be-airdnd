package kr.kro.airbob.domain.wishlist.dto.projection;

import kr.kro.airbob.domain.accommodation.common.AmenityType;

public record WishlistAmenityProjection(
	Long wishlistAccommodationId,
	AmenityType type,
	Integer count
) {
}
