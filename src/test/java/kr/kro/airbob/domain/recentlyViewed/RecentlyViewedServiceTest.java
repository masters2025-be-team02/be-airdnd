package kr.kro.airbob.domain.recentlyViewed;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import kr.kro.airbob.domain.accommodation.common.AmenityType;
import kr.kro.airbob.domain.accommodation.dto.AccommodationResponse;
import kr.kro.airbob.domain.accommodation.repository.AccommodationAmenityRepository;
import kr.kro.airbob.domain.accommodation.repository.AccommodationRepository;
import kr.kro.airbob.domain.recentlyViewed.projection.RecentlyViewedAmenityProjection;
import kr.kro.airbob.domain.recentlyViewed.projection.RecentlyViewedProjection;
import kr.kro.airbob.domain.wishlist.repository.WishlistAccommodationRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("최근 조회 서비스 테스트")
class RecentlyViewedServiceTest {

	@InjectMocks
	private RecentlyViewedService recentlyViewedService;

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Mock
	private AccommodationRepository accommodationRepository;

	@Mock
	private AccommodationAmenityRepository accommodationAmenityRepository;

	@Mock
	private WishlistAccommodationRepository wishlistAccommodationRepository;

	@Mock
	private ZSetOperations<String, String> zSetOperations;

	private static final String RECENTLY_VIEWED_KEY_PREFIX = "recently_viewed:";
	private static final int MAX_COUNT = 100;
	private static final long TTL_DAYS = 7;

	@BeforeEach
	void setUp() {
		given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
	}

	@Nested
	@DisplayName("최근 조회 내역 추가 테스트")
	class AddRecentlyViewedTest {

		@Test
		@DisplayName("새로운 숙소를 최근 조회 내역에 추가한다")
		void addRecentlyViewed_NewAccommodation() {
			// given
			Long memberId = 1L;
			Long accommodationId = 100L;
			String key = RECENTLY_VIEWED_KEY_PREFIX + memberId;

			given(zSetOperations.size(key)).willReturn(10L);

			// when
			recentlyViewedService.addRecentlyViewed(memberId, accommodationId);

			// then
			ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<Double> scoreCaptor = ArgumentCaptor.forClass(Double.class);

			verify(zSetOperations).add(keyCaptor.capture(), valueCaptor.capture(), scoreCaptor.capture());
			verify(redisTemplate).expire(eq(key), eq(Duration.ofDays(TTL_DAYS)));

			assertThat(keyCaptor.getValue()).isEqualTo(key);
			assertThat(valueCaptor.getValue()).isEqualTo(accommodationId.toString());
			assertThat(scoreCaptor.getValue()).isCloseTo(System.currentTimeMillis(), within(1000.0));
		}

		@Test
		@DisplayName("최대 개수를 초과하면 오래된 항목을 제거한다")
		void addRecentlyViewed_ExceedsMaxCount() {
			// given
			Long memberId = 1L;
			Long accommodationId = 100L;
			String key = RECENTLY_VIEWED_KEY_PREFIX + memberId;

			given(zSetOperations.size(key)).willReturn(101L); // MAX_COUNT + 1

			// when
			recentlyViewedService.addRecentlyViewed(memberId, accommodationId);

			// then
			verify(zSetOperations).add(eq(key), eq(accommodationId.toString()), anyDouble());
			verify(zSetOperations).removeRange(eq(key), eq(0L), eq(0L)); // 가장 오래된 1개 제거
			verify(redisTemplate).expire(eq(key), eq(Duration.ofDays(TTL_DAYS)));
		}

		@Test
		@DisplayName("크기가 null이어도 정상적으로 처리한다")
		void addRecentlyViewed_NullSize() {
			// given
			Long memberId = 1L;
			Long accommodationId = 100L;
			String key = RECENTLY_VIEWED_KEY_PREFIX + memberId;

			given(zSetOperations.size(key)).willReturn(null);

			// when
			recentlyViewedService.addRecentlyViewed(memberId, accommodationId);

			// then
			verify(zSetOperations).add(eq(key), eq(accommodationId.toString()), anyDouble());
			verify(zSetOperations, never()).removeRange(any(), anyLong(), anyLong());
			verify(redisTemplate).expire(eq(key), eq(Duration.ofDays(TTL_DAYS)));
		}
	}

	@Nested
	@DisplayName("최근 조회 내역 삭제 테스트")
	class RemoveRecentlyViewedTest {

		@Test
		@DisplayName("특정 숙소를 최근 조회 내역에서 삭제한다")
		void removeRecentlyViewed() {
			// given
			Long memberId = 1L;
			Long accommodationId = 100L;
			String key = RECENTLY_VIEWED_KEY_PREFIX + memberId;

			// when
			recentlyViewedService.removeRecentlyViewed(memberId, accommodationId);

			// then
			verify(zSetOperations).remove(key, accommodationId.toString());
		}
	}

	@Nested
	@DisplayName("최근 조회 내역 조회 테스트")
	class GetRecentlyViewedTest {

		@Test
		@DisplayName("최근 조회 내역을 올바르게 조회한다")
		void getRecentlyViewed_Success() {
			// given
			Long memberId = 1L;
			String key = RECENTLY_VIEWED_KEY_PREFIX + memberId;

			// Redis에서 반환할 데이터 설정
			Set<ZSetOperations.TypedTuple<String>> mockTuples = Set.of(
				createTypedTuple("100", System.currentTimeMillis() - 1000),
				createTypedTuple("101", System.currentTimeMillis() - 3600000)
			);

			given(zSetOperations.reverseRangeWithScores(key, 0, -1)).willReturn(mockTuples);

			// Repository에서 반환할 데이터 설정
			List<RecentlyViewedProjection> accommodations = List.of(
				createRecentlyViewedProjection(100L, "서울 중심가 원룸", "image1.jpg", 4.5),
				createRecentlyViewedProjection(101L, "강남역 스튜디오", "image2.jpg", 4.2)
			);
			given(accommodationRepository.findRecentlyViewedProjectionByIds(anyList()))
				.willReturn(accommodations);

			// 편의시설 데이터
			List<RecentlyViewedAmenityProjection> amenities = List.of(
				createAmenityProjection(100L, AmenityType.WIFI, 1),
				createAmenityProjection(100L, AmenityType.PARKING, 1),
				createAmenityProjection(101L, AmenityType.KITCHEN, 1)
			);
			given(accommodationAmenityRepository.findRecentlyViewedAmenityProjectionByAccommodationIds(anyList()))
				.willReturn(amenities);

			// 위시리스트 데이터
			given(wishlistAccommodationRepository.findAccommodationIdsByMemberIdAndAccommodationIds(
				eq(memberId), anyList())).willReturn(Set.of(100L));

			// when
			AccommodationResponse.RecentlyViewedAccommodations result =
				recentlyViewedService.getRecentlyViewed(memberId);

			// then
			assertThat(result.accommodations()).hasSize(2);
			assertThat(result.totalCount()).isEqualTo(2);

			// ID 존재 여부만 확인 (순서 무관)
			List<Long> accommodationIds = result.accommodations().stream()
				.map(AccommodationResponse.RecentlyViewedAccommodation::accommodationId)
				.toList();
			assertThat(accommodationIds).containsExactlyInAnyOrder(100L, 101L);

			// 각 숙소별 데이터 검증 (ID로 찾아서 검증)
			AccommodationResponse.RecentlyViewedAccommodation accommodation100 = result.accommodations().stream()
				.filter(acc -> acc.accommodationId().equals(100L))
				.findFirst()
				.orElseThrow();

			AccommodationResponse.RecentlyViewedAccommodation accommodation101 = result.accommodations().stream()
				.filter(acc -> acc.accommodationId().equals(101L))
				.findFirst()
				.orElseThrow();

			// 100번 숙소 검증
			assertThat(accommodation100.accommodationName()).isEqualTo("서울 중심가 원룸");
			assertThat(accommodation100.thumbnailUrl()).isEqualTo("image1.jpg");
			assertThat(accommodation100.averageRating()).isEqualTo(4.5);
			assertThat(accommodation100.isInWishlist()).isTrue();
			assertThat(accommodation100.amenities()).hasSize(2);

			// 101번 숙소 검증
			assertThat(accommodation101.accommodationName()).isEqualTo("강남역 스튜디오");
			assertThat(accommodation101.isInWishlist()).isFalse();
			assertThat(accommodation101.amenities()).hasSize(1);
		}

		@Test
		@DisplayName("최근 조회 내역이 비어있으면 빈 결과를 반환한다")
		void getRecentlyViewed_Empty() {
			// given
			Long memberId = 1L;
			String key = RECENTLY_VIEWED_KEY_PREFIX + memberId;

			given(zSetOperations.reverseRangeWithScores(key, 0, -1)).willReturn(null);

			// when
			AccommodationResponse.RecentlyViewedAccommodations result =
				recentlyViewedService.getRecentlyViewed(memberId);

			// then
			assertThat(result.accommodations()).isEmpty();
			assertThat(result.totalCount()).isEqualTo(0);

			verify(accommodationRepository, never()).findRecentlyViewedProjectionByIds(any());
			verify(accommodationAmenityRepository, never()).findRecentlyViewedAmenityProjectionByAccommodationIds(any());
			verify(wishlistAccommodationRepository, never()).findAccommodationIdsByMemberIdAndAccommodationIds(any(), any());
		}

		@Test
		@DisplayName("Redis에는 있지만 DB에서 삭제된 숙소는 필터링된다")
		void getRecentlyViewed_FilterDeletedAccommodations() {
			// given
			Long memberId = 1L;
			String key = RECENTLY_VIEWED_KEY_PREFIX + memberId;

			// Redis에는 3개 숙소가 있음 (최신순으로 정렬)
			Set<ZSetOperations.TypedTuple<String>> mockTuples = Set.of(
				createTypedTuple("100", System.currentTimeMillis() - 1000), // 가장 최근
				createTypedTuple("101", System.currentTimeMillis() - 2000),
				createTypedTuple("102", System.currentTimeMillis() - 3000) // 가장 오래됨 (삭제된 숙소)
			);

			given(zSetOperations.reverseRangeWithScores(key, 0, -1)).willReturn(mockTuples);

			// DB에는 2개 숙소만 존재 (102번은 삭제됨)
			List<RecentlyViewedProjection> accommodations = List.of(
				createRecentlyViewedProjection(100L, "서울 중심가 원룸", "image1.jpg", 4.5),
				createRecentlyViewedProjection(101L, "강남역 스튜디오", "image2.jpg", 4.2)
			);
			given(accommodationRepository.findRecentlyViewedProjectionByIds(anyList()))
				.willReturn(accommodations);

			given(accommodationAmenityRepository.findRecentlyViewedAmenityProjectionByAccommodationIds(any()))
				.willReturn(List.of());
			given(wishlistAccommodationRepository.findAccommodationIdsByMemberIdAndAccommodationIds(any(), any()))
				.willReturn(Set.of());

			// when
			AccommodationResponse.RecentlyViewedAccommodations result =
				recentlyViewedService.getRecentlyViewed(memberId);

			// then
			assertThat(result.accommodations()).hasSize(2); // 삭제된 숙소는 제외
			assertThat(result.totalCount()).isEqualTo(2);

			// 순서에 관계없이 ID만 확인
			List<Long> resultIds = result.accommodations().stream()
				.map(AccommodationResponse.RecentlyViewedAccommodation::accommodationId)
				.toList();
			assertThat(resultIds).containsExactlyInAnyOrder(100L, 101L); // 순서 무관하게 검증
		}

		@Test
		@DisplayName("편의시설과 위시리스트 정보가 올바르게 매핑된다")
		void getRecentlyViewed_CorrectMapping() {
			// given
			Long memberId = 1L;
			String key = RECENTLY_VIEWED_KEY_PREFIX + memberId;

			Set<ZSetOperations.TypedTuple<String>> mockTuples = Set.of(
				createTypedTuple("100", System.currentTimeMillis())
			);

			given(zSetOperations.reverseRangeWithScores(key, 0, -1)).willReturn(mockTuples);

			List<RecentlyViewedProjection> accommodations = List.of(
				createRecentlyViewedProjection(100L, "테스트 숙소", "image.jpg", 4.0)
			);
			given(accommodationRepository.findRecentlyViewedProjectionByIds(anyList()))
				.willReturn(accommodations);

			// 편의시설: WiFi 2개, 주차장 1개
			List<RecentlyViewedAmenityProjection> amenities = List.of(
				createAmenityProjection(100L, AmenityType.WIFI, 2),
				createAmenityProjection(100L, AmenityType.PARKING, 1)
			);
			given(accommodationAmenityRepository.findRecentlyViewedAmenityProjectionByAccommodationIds(anyList()))
				.willReturn(amenities);

			// 위시리스트에 포함됨
			given(wishlistAccommodationRepository.findAccommodationIdsByMemberIdAndAccommodationIds(
				eq(memberId), anyList())).willReturn(Set.of(100L));

			// when
			AccommodationResponse.RecentlyViewedAccommodations result =
				recentlyViewedService.getRecentlyViewed(memberId);

			// then
			AccommodationResponse.RecentlyViewedAccommodation accommodation = result.accommodations().get(0);

			assertThat(accommodation.amenities()).hasSize(2);
			assertThat(accommodation.amenities()).extracting("type")
				.containsExactlyInAnyOrder(AmenityType.WIFI, AmenityType.PARKING);
			assertThat(accommodation.amenities()).extracting("count")
				.containsExactlyInAnyOrder(2, 1);
			assertThat(accommodation.isInWishlist()).isTrue();
		}
	}

	// Helper methods
	private ZSetOperations.TypedTuple<String> createTypedTuple(String value, long timestamp) {
		return new ZSetOperations.TypedTuple<String>() {
			@Override
			public int compareTo(@NotNull ZSetOperations.TypedTuple<String> o) {
				return 0;
			}

			@Override
			public String getValue() {
				return value;
			}

			@Override
			public Double getScore() {
				return (double) timestamp;
			}
		};
	}

	private RecentlyViewedProjection createRecentlyViewedProjection(Long id, String name, String thumbnailUrl, Double rating) {
		return new RecentlyViewedProjection(id, name, thumbnailUrl, rating);
	}

	private RecentlyViewedAmenityProjection createAmenityProjection(Long accommodationId, AmenityType type, Integer count) {
		return new RecentlyViewedAmenityProjection(accommodationId, type, count);
	}
}
