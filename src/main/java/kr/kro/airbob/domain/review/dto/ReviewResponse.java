package kr.kro.airbob.domain.review.dto;

import java.util.List;

import kr.kro.airbob.domain.member.dto.MemberResponse;

public class ReviewResponse {

	private ReviewResponse() {
	}

	public record CreateResponse(
		long id
	) {
	}

	public record UpdateResponse(
		long id
	) {
	}

	public record ReviewInfos(
		List<ReviewInfo> reviews,
		int totalCount
	) {
	}

	public record ReviewInfo(
		long id,
		int rating,
		String content,
		MemberResponse.ReviewerInfo reviewer
	) {
	}
}
