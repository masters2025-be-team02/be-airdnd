package kr.kro.airbob.search.service;

import static kr.kro.airbob.search.event.AccommodationIndexingEvents.*;

import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.kro.airbob.dlq.service.DeadLetterQueueService;
import kr.kro.airbob.search.document.AccommodationDocument;
import kr.kro.airbob.search.repository.AccommodationSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccommodationIndexingService {

	private final AccommodationSearchRepository searchRepository;
	private final AccommodationDocumentBuilder documentBuilder;
	private final AccommodationIndexUpdater indexUpdater;
	private final DeadLetterQueueService dlqService;

	@EventListener
	@Async
	@Transactional(readOnly = true)
	@Retryable(
		//todo: 모든 handle 메서드 구체적 예외 적용 필요
		retryFor = {Exception.class},
		// retryFor = {ElasticsearchException.class, ConnectException.class, TimeoutException.class},
		// noRetryFor = {}
		maxAttempts = 3,
		backoff = @Backoff(delay = 1000, multiplier = 2)
	)
	public void handleAccommodationCreated(AccommodationCreatedEvent event) {
		AccommodationDocument document = documentBuilder.buildAccommodationDocument(event.accommodationId());
		searchRepository.save(document);
	}

	@Recover
	public void recoverAccommodationCreated(Exception e, AccommodationCreatedEvent event) {
		log.error("숙소 생성 색인 최종 실패: accommodationId={}, error={}",
			event.accommodationId(), e.getMessage(), e);

		dlqService.saveFailedEvent("AccommodationCreatedEvent", event, e);
	}

	@EventListener
	@Async
	@Transactional(readOnly = true)
	@Retryable(
		//todo: 모든 handle 메서드 구체적 예외 적용 필요
		retryFor = {Exception.class},
		// retryFor = {ElasticsearchException.class, ConnectException.class, TimeoutException.class},
		// noRetryFor = {}
		maxAttempts = 3,
		backoff = @Backoff(delay = 1000, multiplier = 2)
	)
	public void handleAccommodationUpdated(AccommodationUpdatedEvent event) {
		AccommodationDocument document = documentBuilder.buildAccommodationDocument(event.accommodationId());
		searchRepository.save(document);
	}

	@Recover
	public void recoverAccommodationUpdated(Exception e, AccommodationUpdatedEvent event) {
		log.error("숙소 수정 색인 최종 실패: accommodationId={}, error={}",
			event.accommodationId(), e.getMessage(), e);

		dlqService.saveFailedEvent("AccommodationUpdatedEvent", event, e);
	}

	@EventListener
	@Async
	@Retryable(
		//todo: 모든 handle 메서드 구체적 예외 적용 필요
		retryFor = {Exception.class},
		// retryFor = {ElasticsearchException.class, ConnectException.class, TimeoutException.class},
		// noRetryFor = {}
		maxAttempts = 3,
		backoff = @Backoff(delay = 1000, multiplier = 2)
	)
	public void handleAccommodationDeleted(AccommodationDeletedEvent event) {
		searchRepository.deleteById(event.accommodationId());
	}

	@Recover
	public void recoverAccommodationDeleted(Exception e, AccommodationDeletedEvent event) {
		log.error("숙소 삭제 색인 최종 실패: accommodationId={}, error={}",
			event.accommodationId(), e.getMessage(), e);

		dlqService.saveFailedEvent("AccommodationDeletedEvent", event, e);
	}

	// 리뷰 변경 이벤트 (생성/삭제)
	@EventListener
	@Async
	@Transactional(readOnly = true)
	@Retryable(
		//todo: 모든 handle 메서드 구체적 예외 적용 필요
		retryFor = {Exception.class},
		// retryFor = {ElasticsearchException.class, ConnectException.class, TimeoutException.class},
		// noRetryFor = {}
		maxAttempts = 3,
		backoff = @Backoff(delay = 1000, multiplier = 2)
	)
	public void handleReviewChanged(ReviewSummaryChangedEvent event) {
		indexUpdater.updateReviewSummaryInIndex(event.accommodationId());
	}

	@Recover
	public void recoverReviewSummaryChanged(Exception e, ReviewSummaryChangedEvent event) {
		log.error("리뷰 요약 색인 최종 실패: accommodationId={}, error={}",
			event.accommodationId(), e.getMessage(), e);

		dlqService.saveFailedEvent("ReviewSummaryChangedEvent", event, e);
	}

	// 예약 변경 이벤트 (생성/삭제)
	@EventListener
	@Async
	@Transactional(readOnly = true)
	@Retryable(
		retryFor = {Exception.class},
		maxAttempts = 3,
		backoff = @Backoff(delay = 1000, multiplier = 2)
	)
	public void handleReservationChanged(ReservationChangedEvent event) {
		indexUpdater.updateReservedDatesInIndex(event.accommodationId());
	}

	@Recover
	public void recoverReservationChanged(Exception e, ReservationChangedEvent event) {
		log.error("예약 변경 색인 최종 실패: accommodationId={}, error={}",
			event.accommodationId(), e.getMessage(), e);
		dlqService.saveFailedEvent("ReservationChangedEvent", event, e);
	}
}
