package kr.kro.airbob.search.event;

import java.time.LocalDate;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccommodationIndexingEvents {

	// 숙소 이벤트
	public record AccommodationCreatedEvent(Long accommodationId){}
	public record AccommodationUpdatedEvent(Long accommodationId){}
	public record AccommodationDeletedEvent(Long accommodationId){}

	// 리뷰 이벤트
	public record ReviewSummaryChangedEvent(Long accommodationId) {}

	// 예약 이벤트
	public record ReservationCreatedEvent(Long accommodationId, List<LocalDate> addedDates){}
	public record ReservationDeletedEvent(Long accommodationId, List<LocalDate> removedDates){}
}
