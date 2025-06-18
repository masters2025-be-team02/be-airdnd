package kr.kro.airbob.domain.review;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.kro.airbob.cursor.annotation.CursorParam;
import kr.kro.airbob.cursor.dto.CursorRequest;
import kr.kro.airbob.cursor.annotation.CursorParam;
import kr.kro.airbob.cursor.dto.CursorRequest;
import kr.kro.airbob.domain.review.ReviewService;
import kr.kro.airbob.domain.review.ReviewSortType;
import kr.kro.airbob.domain.review.dto.ReviewRequest;
import kr.kro.airbob.domain.review.dto.ReviewResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accommodations/{accommodationId}/reviews")
public class ReviewController {

	private final ReviewService reviewService;

	@PostMapping
	public ResponseEntity<ReviewResponse.CreateResponse> createReview(
		@PathVariable Long accommodationId,
		@Valid @RequestBody ReviewRequest.CreateRequest requestDto,
		HttpServletRequest request) {

		Long memberId = (Long)request.getAttribute("memberId");

		ReviewResponse.CreateResponse response =
			reviewService.createReview(accommodationId, requestDto, memberId);

		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{reviewId}/content")
	public ResponseEntity<ReviewResponse.UpdateResponse> updateContentResponse(
		@PathVariable Long reviewId,
		@Valid @RequestBody ReviewRequest.UpdateContentRequest requestDto) {

		ReviewResponse.UpdateResponse response =
			reviewService.updateContentReview(reviewId, requestDto);

		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{reviewId}/rating")
	public ResponseEntity<ReviewResponse.UpdateResponse> updateRatingResponse(
		@PathVariable Long reviewId,
		@Valid @RequestBody ReviewRequest.UpdateRatingRequest requestDto) {

		ReviewResponse.UpdateResponse response =
			reviewService.updateRatingReview(reviewId, requestDto);

		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{reviewId}")
	@ResponseStatus(HttpStatus.OK)
	public void deleteReview(@PathVariable Long reviewId) {
		reviewService.deleteReview(reviewId);
	}

	@GetMapping
	public ResponseEntity<ReviewResponse.ReviewInfos> findReviews(
		@PathVariable Long accommodationId,
		@RequestParam(defaultValue = "LATEST") ReviewSortType sortType,
		@CursorParam CursorRequest.ReviewCursorPageRequest cursorRequest) {

		ReviewResponse.ReviewInfos response =
			reviewService.findReviews(accommodationId, cursorRequest, sortType);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/summary")
	public ResponseEntity<ReviewResponse.ReviewSummary> findReviewSummary(@PathVariable Long accommodationId) {

		ReviewResponse.ReviewSummary response =
			reviewService.findReviewSummary(accommodationId);

		return ResponseEntity.ok(response);
	}
}
