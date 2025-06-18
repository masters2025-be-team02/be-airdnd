package kr.kro.airbob.domain.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ReviewRequest {

	private ReviewRequest() {
	}

	public record CreateRequest(

		@NotNull(message = "평점은 필수입니다.")
		@Positive(message = "평점은 양수여야 합니다.")
		Integer rating,

		@NotBlank(message = "후기 본문은 공백일 수 없습니다.")
		@Size(max = 1024)
		String content
	) {
	}
}
