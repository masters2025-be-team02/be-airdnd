package kr.kro.airbob.domain.review.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.kro.airbob.domain.review.AccommodationReviewSummary;

public interface AccommodationReviewSummaryRepository extends JpaRepository<AccommodationReviewSummary, Long> {

	Optional<AccommodationReviewSummary> findByAccommodationId(Long accommodationId);

	List<AccommodationReviewSummary> findByAccommodationIdIn(List<Long> accommodationId);

}
