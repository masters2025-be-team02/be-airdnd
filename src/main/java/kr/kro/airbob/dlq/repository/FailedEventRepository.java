package kr.kro.airbob.dlq.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.kro.airbob.dlq.entity.FailedIndexingEvent;

public interface FailedEventRepository extends JpaRepository<FailedIndexingEvent, Long> {

	@Query("SELECT f FROM FailedIndexingEvent f WHERE f.status = 'FAILED' " +
		"AND f.nextRetryAt <= :now AND f.retryCount < :maxRetries")
	List<FailedIndexingEvent> findEventsReadyForRetry(
		@Param("now") LocalDateTime now,
		@Param("maxRetries") int maxRetries
	);

	@Query("SELECT f FROM FailedIndexingEvent f WHERE f.status = 'FAILED' " +
		"AND f.retryCount >= :maxRetries")
	List<FailedIndexingEvent> findDeadLetterCandidates(@Param("maxRetries") int maxRetries);

}
