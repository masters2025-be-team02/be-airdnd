package kr.kro.airbob.domain.member.dto;

import java.time.LocalDateTime;

public class MemberResponse {

	private MemberResponse() {
	}

	public record ReviewerInfo(
		long id,
		String nickname,
		String thumbnailImageUrl,
		LocalDateTime joinedAt
	) {
	}
}
