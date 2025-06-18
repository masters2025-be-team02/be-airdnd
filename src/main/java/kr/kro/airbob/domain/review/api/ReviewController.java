package kr.kro.airbob.domain.review.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import kr.kro.airbob.domain.review.ReviewService;
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
		ReviewRequest.CreateRequest requestDto,
		HttpServletRequest request) {

		Long memberId = (Long) request.getAttribute("memberId");

		ReviewResponse.CreateResponse response =
			reviewService.createReview(accommodationId, requestDto, memberId);

		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{reviewId}")
	public ResponseEntity<ReviewResponse.UpdateResponse> updateContentResponse(
		@PathVariable Long accommodationId,
		@PathVariable Long reviewId,
		ReviewRequest.UpdateContentRequest requestDto,
		HttpServletRequest request) {

		Long memberId = (Long) request.getAttribute("memberId");

		ReviewResponse.UpdateResponse response =
			reviewService.updateContentReview(accommodationId, reviewId, requestDto, memberId);

		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{reviewId}")
	public ResponseEntity<ReviewResponse.UpdateResponse> updateRatingResponse(
		@PathVariable Long accommodationId,
		@PathVariable Long reviewId,
		ReviewRequest.UpdateRatingRequest requestDto,
		HttpServletRequest request) {

		Long memberId = (Long) request.getAttribute("memberId");

		ReviewResponse.UpdateResponse response =
			reviewService.updateRatingReview(accommodationId, reviewId, requestDto, memberId);

		return ResponseEntity.ok(response);
	}


}
