package kr.kro.airbob.domain.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	@Query("select r.accommodation.id from Review r where r.id = :reviewId")
	Long findAccommodationIdByReviewId(@Param("reviewId") Long reviewId);
}
