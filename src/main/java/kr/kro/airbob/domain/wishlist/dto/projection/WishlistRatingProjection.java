package kr.kro.airbob.domain.wishlist.dto.projection;


public record WishlistRatingProjection(
	Long wishlistAccommodationId,
	Double averageRating
) {
}
