package kr.kro.airbob.domain.review.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.kro.airbob.cursor.dto.CursorRequest;
import kr.kro.airbob.cursor.dto.CursorResponse;
import kr.kro.airbob.cursor.util.CursorPageInfoCreator;
import kr.kro.airbob.domain.accommodation.entity.Accommodation;
import kr.kro.airbob.domain.accommodation.exception.AccommodationNotFoundException;
import kr.kro.airbob.domain.accommodation.repository.AccommodationRepository;
import kr.kro.airbob.domain.member.Member;
import kr.kro.airbob.domain.member.MemberRepository;
import kr.kro.airbob.domain.member.exception.MemberNotFoundException;
import kr.kro.airbob.domain.review.Review;
import kr.kro.airbob.domain.review.ReviewRepository;
import kr.kro.airbob.domain.review.ReviewSortType;
import kr.kro.airbob.domain.review.dto.ReviewRequest;
import kr.kro.airbob.domain.review.dto.ReviewResponse;
import kr.kro.airbob.domain.review.dto.projection.ReviewProjection;
import kr.kro.airbob.domain.review.exception.ReviewNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final MemberRepository memberRepository;
	private final AccommodationRepository accommodationRepository;

	private final CursorPageInfoCreator cursorPageInfoCreator;

	@Transactional
	public ReviewResponse.CreateResponse createReview(Long accommodationId, ReviewRequest.CreateRequest request, Long memberId) {

		Member author = findMemberById(memberId);
		Accommodation accommodation = findAccommodationById(accommodationId);

		Review review = Review.builder()
			.rating(request.rating())
			.content(request.content())
			.accommodation(accommodation)
			.author(author)
			.build();

		Review savedReview = reviewRepository.save(review);
		return new ReviewResponse.CreateResponse(savedReview.getId());
	}

	@Transactional
	public ReviewResponse.UpdateResponse updateContentReview(Long reviewId, ReviewRequest.UpdateContentRequest request) {
		Review review = findReviewById(reviewId);
		review.updateContent(request.content());
		return new ReviewResponse.UpdateResponse(review.getId());
	}

	@Transactional
	public ReviewResponse.UpdateResponse updateRatingReview(Long reviewId, ReviewRequest.UpdateRatingRequest request) {
		Review review = findReviewById(reviewId);
		review.updateRating(request.rating());
		return new ReviewResponse.UpdateResponse(review.getId());
	}

	@Transactional
	public void deleteReview(Long reviewId) {
		Review review = findReviewById(reviewId);
		reviewRepository.delete(review);
	}

	@Transactional(readOnly = true)
	public ReviewResponse.ReviewInfos findReviews(
		Long accommodationId,
		CursorRequest.ReviewCursorPageRequest cursorRequest,
		ReviewSortType sortType) {

		PageRequest pageRequest = PageRequest.of(0, cursorRequest.size());
		Long lastId = cursorRequest.lastId();
		LocalDateTime lastCreatedAt = cursorRequest.lastCreatedAt();
		Integer lastRating = cursorRequest.lastRating();

		Slice<ReviewProjection> reviewSlice = reviewRepository.findByAccommodationIdWithCursor(
			accommodationId, lastId, lastCreatedAt, lastRating, sortType, pageRequest);

		List<ReviewResponse.ReviewInfo> reviewInfos = reviewSlice.getContent()
			.stream()
			.map(ReviewResponse.ReviewInfo::from)
			.toList();

		CursorResponse.PageInfo pageInfo = cursorPageInfoCreator.createPageInfo(
			reviewSlice.getContent(),
			reviewSlice.hasNext(),
			ReviewProjection::reviewId,
			ReviewProjection::reviewedAt,
			ReviewProjection::rating
		);

		return new ReviewResponse.ReviewInfos(reviewInfos, pageInfo);
	}

	private Member findMemberById(Long memberId) {
		return memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
	}

	private Accommodation findAccommodationById(Long accommodationId) {
		return accommodationRepository.findById(accommodationId).orElseThrow(AccommodationNotFoundException::new);
	}

	private Review findReviewById(Long reviewId) {
		return reviewRepository.findById(reviewId).orElseThrow(ReviewNotFoundException::new);
	}
}
