package kr.kro.airbob.domain.review.dto;

import java.time.LocalDateTime;
import java.util.List;

import kr.kro.airbob.cursor.dto.CursorResponse;
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
		CursorResponse.PageInfo pageInfo

	) {
	}

	public record ReviewInfo(
		long id,
		int rating,
		String content,
		LocalDateTime reviewedAt,
		MemberResponse.ReviewerInfo reviewer
	) {
	}
}
