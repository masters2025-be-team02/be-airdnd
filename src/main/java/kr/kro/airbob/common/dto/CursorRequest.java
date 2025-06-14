package kr.kro.airbob.common.dto;

import java.time.LocalDateTime;

import lombok.Builder;

public class CursorRequest {

	private CursorRequest() {
	}

	@Builder
	public record CursorPageRequest(
		Integer size,
		Long lastId,
		LocalDateTime lastCreatedAt
	) {
		public CursorPageRequest{
			if (size == null) {
				size = 20; // 기본값
			}
		}
	}
}
