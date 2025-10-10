package kr.kro.airbob.domain.recentlyViewed;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import kr.kro.airbob.domain.accommodation.dto.AccommodationResponse;
import kr.kro.airbob.domain.accommodation.entity.Accommodation;
import kr.kro.airbob.domain.accommodation.entity.AccommodationAmenity;
import kr.kro.airbob.domain.accommodation.repository.AccommodationAmenityRepository;
import kr.kro.airbob.domain.accommodation.repository.AccommodationRepository;
import kr.kro.airbob.domain.review.AccommodationReviewSummary;
import kr.kro.airbob.domain.review.repository.AccommodationReviewSummaryRepository;
import kr.kro.airbob.domain.wishlist.repository.WishlistAccommodationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecentlyViewedService {

	private final RedisTemplate<String, String> redisTemplate;
	private final AccommodationRepository accommodationRepository;
	private final AccommodationReviewSummaryRepository summaryRepository;
	private final AccommodationAmenityRepository accommodationAmenityRepository;
	private final WishlistAccommodationRepository wishlistAccommodationRepository;

	private static final String RECENTLY_VIEWED_KEY_PREFIX = "recently_viewed:";
	private static final int MAX_COUNT = 100;
	private static final long TTL_DAYS = 7;

	public void addRecentlyViewed(Long memberId, Long accommodationId) {
		String key = RECENTLY_VIEWED_KEY_PREFIX + memberId;
		long timestamp = System.currentTimeMillis();

		redisTemplate.opsForZSet().add(key, accommodationId.toString(), timestamp);

		Long currentSize = redisTemplate.opsForZSet().size(key);
		if (currentSize != null && currentSize > MAX_COUNT) {
			redisTemplate.opsForZSet().removeRange(key, 0, currentSize - MAX_COUNT - 1);
		}
		redisTemplate.expire(key, Duration.ofDays(TTL_DAYS));
	}

	public void removeRecentlyViewed(Long memberId, Long accommodationId) {
		String key = RECENTLY_VIEWED_KEY_PREFIX + memberId;
		redisTemplate.opsForZSet().remove(key, accommodationId.toString());
	}

	public AccommodationResponse.RecentlyViewedAccommodations getRecentlyViewed(Long memberId) {
		String key = RECENTLY_VIEWED_KEY_PREFIX + memberId;

		Set<ZSetOperations.TypedTuple<String>> recentlyViewedWithScores = redisTemplate.opsForZSet()
			.reverseRangeWithScores(key, 0, -1);

		if (recentlyViewedWithScores == null || recentlyViewedWithScores.isEmpty()) {
			return AccommodationResponse.RecentlyViewedAccommodations.builder()
				.accommodations(new ArrayList<>())
				.totalCount(0)
				.build();
		}

		List<Long> accommodationIds = recentlyViewedWithScores.stream()
			.map(tuple -> Long.parseLong(tuple.getValue()))
			.toList();

		// 숙소 조회
		List<Accommodation> accommodations = accommodationRepository.findByIdIn(accommodationIds);

		// 순서를 위한 Map
		Map<Long, Accommodation> accommodationMap = accommodations.stream()
			.collect(Collectors.toMap(Accommodation::getId, accommodation -> accommodation));

		// 숙소 리뷰 개수
		Map<Long, BigDecimal> reviewRatingMap = getReviewRatingMap(accommodationIds);

		// 어매니티
		Map<Long, List<AccommodationResponse.AmenityInfoResponse>> amenityMap = getAmenityMap(accommodationIds);

		// 위시리스트 여부
		Map<Long, Boolean> wishlistMap = getWishlistMap(memberId, accommodationIds);

		// 조합
		List<AccommodationResponse.RecentlyViewedAccommodation> recentlyViewedAccommodations = recentlyViewedWithScores.stream()
			.map(tuple -> {
				Long accommodationId = Long.parseLong(tuple.getValue());
				Accommodation accommodation = accommodationMap.get(accommodationId);

				if (accommodation == null) {
					// 삭제된 숙소인 경우 redis에서도 제거
					redisTemplate.opsForZSet().remove(key, accommodationId.toString());
					return null;
				}

				// 조회 시각 (score)
				LocalDateTime viewAt = Instant.ofEpochMilli(tuple.getScore().longValue())
					.atZone(ZoneId.systemDefault())
					.toLocalDateTime();

				return AccommodationResponse.RecentlyViewedAccommodation.builder()
					.viewedAt(viewAt)
					.accommodationId(accommodationId)
					.accommodationName(accommodation.getName())
					.thumbnailUrl(accommodation.getThumbnailUrl())
					.amenities(amenityMap.getOrDefault(accommodationId, new ArrayList<>()))
					.averageRating(reviewRatingMap.get(accommodationId))
					.isInWishlist(wishlistMap.getOrDefault(accommodationId, false))
					.build();
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		return AccommodationResponse.RecentlyViewedAccommodations.builder()
			.accommodations(recentlyViewedAccommodations)
			.totalCount(recentlyViewedAccommodations.size())
			.build();
	}

	private Map<Long, Boolean> getWishlistMap(Long memberId, List<Long> accommodationIds) {
		Set<Long> wishlistAccommodationIds = wishlistAccommodationRepository
			.findAccommodationIdsByMemberIdAndAccommodationIds(memberId, accommodationIds);

		return accommodationIds.stream()
			.collect(Collectors.toMap(
				id -> id,
				wishlistAccommodationIds::contains
			));
	}

	private Map<Long, BigDecimal> getReviewRatingMap(List<Long> accommodationIds) {
		List<AccommodationReviewSummary> summaries = summaryRepository.findByAccommodationIdIn(
			accommodationIds);

		return summaries.stream()
			.collect(Collectors.toMap(
				AccommodationReviewSummary::getAccommodationId,
				AccommodationReviewSummary::getAverageRating
			));
	}


	private Map<Long, List<AccommodationResponse.AmenityInfoResponse>> getAmenityMap(
		List<Long> accommodationIds) {

		List<AccommodationAmenity> results =
			accommodationAmenityRepository.findAccommodationAmenitiesByAccommodationIds(accommodationIds);

		return results.stream()
			.collect(Collectors.groupingBy(
				aa -> aa.getAccommodation().getId(),
				Collectors.mapping(
					result -> new AccommodationResponse.AmenityInfoResponse(
						result.getAmenity().getName(),
						result.getCount()
					),
					Collectors.toList()
				)));
	}
}
