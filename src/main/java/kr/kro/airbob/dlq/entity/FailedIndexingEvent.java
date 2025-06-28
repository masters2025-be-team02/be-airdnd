package kr.kro.airbob.dlq.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.kro.airbob.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@Table(name = "failed_indexing_events")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FailedIndexingEvent extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String eventType;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String eventData;

	@Column(columnDefinition = "TEXT")
	private String errorMessage;

	@Column(nullable = false)
	private LocalDateTime failedAt;

	@Column(nullable = false)
	@Builder.Default
	private int retryCount = 0;

	@Column(nullable = false)
	private LocalDateTime nextRetryAt; // 다음 재시도 시점

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private EventStatus status = EventStatus.FAILED;

	private LocalDateTime lastRetryAt;

	public static FailedIndexingEvent create(String eventType, String serializedEventData, String errorMessage) {

		LocalDateTime now = LocalDateTime.now();

		return FailedIndexingEvent.builder()
			.eventType(eventType)
			.eventData(serializedEventData)
			.errorMessage(errorMessage)
			.failedAt(now)
			.nextRetryAt(now.plusMinutes(5))
			.build();
	}

	public void incrementRetryCount() {

		LocalDateTime now = LocalDateTime.now();

		this.retryCount++;
		this.lastRetryAt = now;
		this.nextRetryAt = now.plusMinutes(5L * (long)Math.pow(3, retryCount));
	}

	public void markAsDeadLetter() {
		this.status = EventStatus.DEAD_LETTER;
	}

	public void updateStatus(EventStatus status) {
		this.status = status;
	}

	public void updateErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public enum EventStatus{
		FAILED, RETRYING, DEAD_LETTER, PROCESSED
	}
}
