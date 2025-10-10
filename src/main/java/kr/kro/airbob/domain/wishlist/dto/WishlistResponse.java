package kr.kro.airbob.domain.wishlist.dto;

import java.time.LocalDateTime;
import java.util.List;

import kr.kro.airbob.cursor.dto.CursorResponse;
import kr.kro.airbob.domain.accommodation.dto.AccommodationResponse;

public class WishlistResponse {

	private WishlistResponse() {
	}

	public record CreateResponse(
		long id
	) {
	}

	public record UpdateResponse(
		long id
	) {
	}

	public record WishlistInfos(
		List<WishlistInfo> wishlists,
		CursorResponse.PageInfo pageInfo
	) {
	}

	public record WishlistInfo(
		long id,
		String name,
		LocalDateTime createdAt,
		long wishlistItemCount,
		String thumbnailImageUrl
	) {
	}

	public record CreateWishlistAccommodationResponse(
		long id
	) {
	}

	public record UpdateWishlistAccommodationResponse(
		long id
	) {
	}

	public record WishlistAccommodationInfos(
		List<WishlistResponse.WishlistAccommodationInfo> wishlistAccommodations,
		CursorResponse.PageInfo pageInfo
	) {
	}

	public record WishlistAccommodationInfo(
		long id,
		String name,
		AccommodationResponse.WishlistAccommodationInfo accommodationInfo
	) {
	}
}
