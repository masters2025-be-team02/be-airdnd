package kr.kro.airbob.domain.review;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.kro.airbob.domain.accommodation.entity.Accommodation;
import kr.kro.airbob.domain.accommodation.exception.AccommodationNotFoundException;
import kr.kro.airbob.domain.accommodation.repository.AccommodationRepository;
import kr.kro.airbob.domain.member.Member;
import kr.kro.airbob.domain.member.MemberRepository;
import kr.kro.airbob.domain.member.exception.MemberNotFoundException;
import kr.kro.airbob.domain.review.dto.ReviewRequest;
import kr.kro.airbob.domain.review.dto.ReviewResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final MemberRepository memberRepository;
	private final AccommodationRepository accommodationRepository;

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

	private Member findMemberById(Long memberId) {
		return memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
	}

	private Accommodation findAccommodationById(Long accommodationId) {
		return accommodationRepository.findById(accommodationId).orElseThrow(AccommodationNotFoundException::new);
	}
}
