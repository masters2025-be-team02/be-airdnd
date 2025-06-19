package kr.kro.airbob.cursor.dto;

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

		@Override
		public String toString() {
			return "CursorPageRequest{" +
				"size=" + size +
				", lastId=" + lastId +
				", lastCreatedAt=" + lastCreatedAt +
				'}';
		}
	}

	@Builder
	public record ReviewCursorPageRequest(
		Integer size,
		Long lastId,
		LocalDateTime lastCreatedAt,
		Integer lastRating
	) {
	}
}
