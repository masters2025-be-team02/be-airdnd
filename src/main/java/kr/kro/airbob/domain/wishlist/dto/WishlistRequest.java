package kr.kro.airbob.domain.wishlist.dto;

import jakarta.validation.constraints.NotBlank;
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
}
