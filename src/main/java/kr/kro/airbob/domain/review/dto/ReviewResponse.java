package kr.kro.airbob.domain.review.dto;

public class ReviewResponse {

	private ReviewResponse() {
	}

	public record CreateResponse(
		long id
	) {
	}
}
