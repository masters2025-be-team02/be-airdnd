package kr.kro.airbob.domain.wishlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class WishlistRequest {

	private WishlistRequest() {
	}

	public record createRequest(
		@NotBlank(message = "위시리스트 이름은 공백일 수 없습니다.")
		@Size(max = 255)
		String name
	) {
	}

	public record updateRequest(
		@NotBlank(message = "위시리스트 이름은 공백일 수 없습니다.")
		@Size(max = 255)
		String name
	) {
	}

	public record CreateWishlistAccommodationRequest(
		@Positive(message = "숙소 ID는 양수여야 합니다.")
		@NotNull(message = "숙소 ID는 필수입니다.")
		Long accommodationId
	) {
	}

	public record UpdateWishlistAccommodationRequest(
		@NotBlank(message = "메모는 공백일 수 없습니다.")
		@Size(max = 1024)
		String memo
	) {
	}
}
