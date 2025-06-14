package kr.kro.airbob.common.dto;

import java.time.LocalDateTime;

import lombok.Builder;

public class CursorResponse {

	private CursorResponse() {
	}

	@Builder
	public record PageInfo(
		boolean hasNext,
		String nextCursor,
		int currentSize
	) {
	}

	@Builder
	public record CursorData(
		Long id,
		LocalDateTime lastCreatedAt
	) {
	}
}
