package kr.kro.airbob.cursor.dto;

import java.time.LocalDateTime;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CursorResponse {

	@Builder
	public record PageInfo(
		boolean hasNext,
		String nextCursor,
		int currentSize
	) {
	}

	@Builder
	@Getter
	@AllArgsConstructor
	public static class CursorData {
		private final Long id;
		private final LocalDateTime lastCreatedAt;
	}

	@Getter
	public static class ReviewCursorData extends CursorData {

		private final Integer lastRating;

		public ReviewCursorData(Long id, LocalDateTime lastCreatedAt, Integer lastRating) {
			super(id, lastCreatedAt);
			this.lastRating = lastRating;
		}
	}
}
