package kr.kro.airbob.domain.wishlist.dto;

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
}
