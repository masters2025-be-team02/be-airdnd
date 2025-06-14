package kr.kro.airbob.domain.wishlist.dto;

import java.time.LocalDateTime;
import java.util.List;

import kr.kro.airbob.cursor.dto.CursorResponse;

public class WishlistResponse {

	private WishlistResponse() {
	}

	public record createResponse(
		long id
	) {
		@Override
		public String toString() {
			return "createResponse{" +
				"id=" + id +
				'}';
		}
	}

	public record updateResponse(
		long id
	) {
		@Override
		public String toString() {
			return "updateResponse{" +
				"id=" + id +
				'}';
		}
	}

	public record WishlistInfos(
		List<WishlistInfo> wishlists,
		CursorResponse.PageInfo pageInfo
	) {
		@Override
		public String toString() {
			return "ListResponse{" +
				"wishlists=" + wishlists +
				", pageInfo=" + pageInfo +
				'}';
		}
	}

	public record WishlistInfo(
		long id,
		String name,
		LocalDateTime createdAt,
		long wishlistItemCount,
		String thumbnailImageUrl
	) {
	}
}
