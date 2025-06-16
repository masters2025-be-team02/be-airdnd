package kr.kro.airbob.domain.wishlist;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import kr.kro.airbob.cursor.dto.CursorRequest;
import kr.kro.airbob.cursor.dto.CursorResponse;
import kr.kro.airbob.cursor.util.CursorPageInfoCreator;
import kr.kro.airbob.domain.accommodation.common.AmenityType;
import kr.kro.airbob.domain.accommodation.entity.Accommodation;
import kr.kro.airbob.domain.accommodation.exception.AccommodationNotFoundException;
import kr.kro.airbob.domain.accommodation.repository.AccommodationRepository;
import kr.kro.airbob.domain.member.Member;
import kr.kro.airbob.domain.member.MemberRepository;
import kr.kro.airbob.domain.member.common.MemberRole;
import kr.kro.airbob.domain.member.exception.MemberNotFoundException;
import kr.kro.airbob.domain.wishlist.dto.WishlistRequest;
import kr.kro.airbob.domain.wishlist.dto.WishlistResponse;
import kr.kro.airbob.domain.wishlist.dto.projection.WishlistAmenityProjection;
import kr.kro.airbob.domain.wishlist.dto.projection.WishlistImageProjection;
import kr.kro.airbob.domain.wishlist.dto.projection.WishlistRatingProjection;
import kr.kro.airbob.domain.wishlist.exception.WishlistAccessDeniedException;
import kr.kro.airbob.domain.wishlist.exception.WishlistAccommodationAccessDeniedException;
import kr.kro.airbob.domain.wishlist.exception.WishlistAccommodationNotFoundException;
import kr.kro.airbob.domain.wishlist.exception.WishlistNotFoundException;
import kr.kro.airbob.domain.wishlist.repository.WishlistAccommodationRepository;
import kr.kro.airbob.domain.wishlist.repository.WishlistRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("위시리스트 서비스 테스트")
class WishlistServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private WishlistRepository wishlistRepository;

	@Mock
	private WishlistAccommodationRepository wishlistAccommodationRepository;

	@Mock
	private CursorPageInfoCreator cursorPageInfoCreator;

	@Mock
	private AccommodationRepository accommodationRepository;

	@InjectMocks
	private WishlistService wishlistService;

	private Member member;
	private Member adminMember;
	private Wishlist wishlist;
	private Wishlist otherWishlist;

	@BeforeEach
	void setUp() {
		// 일반 회원
		member = Member.builder()
			.id(1L)
			.email("test@example.com")
			.nickname("테스트 사용자")
			.role(MemberRole.MEMBER)
			.build();

		// 관리자 회원
		adminMember = Member.builder()
			.id(2L)
			.email("admin@example.com")
			.nickname("관리자")
			.role(MemberRole.ADMIN)
			.build();

		// 다른 회원
		Member otherMember = Member.builder()
			.id(3L)
			.email("other@example.com")
			.nickname("다른 사용자")
			.role(MemberRole.MEMBER)
			.build();

		// 위시리스트
		wishlist = Wishlist.builder()
			.id(1L)
			.name("서울 여행")
			.member(member)
			.build();

		// 다른 사용자의 위시리스트
		otherWishlist = Wishlist.builder()
			.id(2L)
			.name("부산 여행")
			.member(otherMember)
			.build();
	}

	@Nested
	@DisplayName("위시리스트 생성 테스트")
	class CreateWishlistTest {

		@Test
		@DisplayName("정상적으로 위시리스트를 생성한다")
		void createWishlist_Success() {
			// Given
			WishlistRequest.createRequest request = new WishlistRequest.createRequest("서울 여행 계획");
			Long currentMemberId = 1L;

			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));
			given(wishlistRepository.save(any(Wishlist.class))).willAnswer(invocation -> {
				Wishlist savedWishlist = invocation.getArgument(0);
				return Wishlist.builder()
					.id(1L)
					.name(savedWishlist.getName())
					.member(savedWishlist.getMember())
					.build();
			});

			// When
			WishlistResponse.CreateResponse response = wishlistService.createWishlist(request, currentMemberId);

			// Then
			assertThat(response.id()).isEqualTo(1L);

			verify(memberRepository).findById(currentMemberId);
			verify(wishlistRepository).save(any(Wishlist.class));
		}

		@Test
		@DisplayName("존재하지 않는 사용자로 위시리스트 생성 시 예외 발생")
		void createWishlist_MemberNotFound() {
			// Given
			WishlistRequest.createRequest request = new WishlistRequest.createRequest("서울 여행 계획");
			Long nonExistentMemberId = 999L;

			given(memberRepository.findById(nonExistentMemberId)).willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> wishlistService.createWishlist(request, nonExistentMemberId))
				.isInstanceOf(MemberNotFoundException.class)
				.hasMessage("존재하지 않는 사용자입니다.");

			verify(memberRepository).findById(nonExistentMemberId);
			verify(wishlistRepository, never()).save(any(Wishlist.class));
		}

		@Test
		@DisplayName("같은 이름의 위시리스트를 여러 개 생성할 수 있다")
		void createWishlist_DuplicateNameAllowed() {
			// Given
			WishlistRequest.createRequest request = new WishlistRequest.createRequest("중복 이름");
			Long currentMemberId = 1L;

			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));
			given(wishlistRepository.save(any(Wishlist.class)))
				.willAnswer(invocation -> {
					Wishlist savedWishlist = invocation.getArgument(0);
					return Wishlist.builder()
						.id(1L)
						.name(savedWishlist.getName())
						.member(savedWishlist.getMember())
						.build();
				})
				.willAnswer(invocation -> {
					Wishlist savedWishlist = invocation.getArgument(0);
					return Wishlist.builder()
						.id(2L)
						.name(savedWishlist.getName())
						.member(savedWishlist.getMember())
						.build();
				});

			// When
			WishlistResponse.CreateResponse firstResponse = wishlistService.createWishlist(request, currentMemberId);
			WishlistResponse.CreateResponse secondResponse = wishlistService.createWishlist(request, currentMemberId);

			// Then
			assertThat(firstResponse.id()).isEqualTo(1L);
			assertThat(secondResponse.id()).isEqualTo(2L);

			verify(memberRepository, times(2)).findById(currentMemberId);
			verify(wishlistRepository, times(2)).save(any(Wishlist.class));
		}
	}

	@Nested
	@DisplayName("위시리스트 수정 테스트")
	class UpdateWishlistTest {

		@Test
		@DisplayName("정상적으로 위시리스트를 수정한다")
		void updateWishlist_Success() {
			// Given
			Long wishlistId = 1L;
			WishlistRequest.updateRequest request = new WishlistRequest.updateRequest("수정된 서울 여행");
			Long currentMemberId = 1L;

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));

			// When
			WishlistResponse.UpdateResponse response = wishlistService.updateWishlist(wishlistId, request, currentMemberId);

			// Then
			assertThat(response.id()).isEqualTo(wishlistId);
			assertThat(wishlist.getName()).isEqualTo("수정된 서울 여행");

			verify(wishlistRepository).findById(wishlistId);
		}

		@Test
		@DisplayName("존재하지 않는 위시리스트 수정 시 예외 발생")
		void updateWishlist_WishlistNotFound() {
			// Given
			Long nonExistentWishlistId = 999L;
			WishlistRequest.updateRequest request = new WishlistRequest.updateRequest("수정된 이름");
			Long currentMemberId = 1L;

			given(wishlistRepository.findById(nonExistentWishlistId)).willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> wishlistService.updateWishlist(nonExistentWishlistId, request, currentMemberId))
				.isInstanceOf(WishlistNotFoundException.class)
				.hasMessage("존재하지 않는 위시리스트입니다.");

			verify(wishlistRepository).findById(nonExistentWishlistId);
		}

		@Test
		@DisplayName("다른 사용자의 위시리스트 수정 시 예외 발생")
		void updateWishlist_AccessDenied() {
			// Given
			Long wishlistId = 2L;
			WishlistRequest.updateRequest request = new WishlistRequest.updateRequest("수정된 이름");
			Long currentMemberId = 1L; // 다른 사용자 ID

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(otherWishlist));

			// When & Then
			assertThatThrownBy(() -> wishlistService.updateWishlist(wishlistId, request, currentMemberId))
				.isInstanceOf(WishlistAccessDeniedException.class)
				.hasMessage("위시리스트에 대한 접근 권한이 없습니다.");

			verify(wishlistRepository).findById(wishlistId);
		}

		@Test
		@DisplayName("같은 이름으로 수정할 수 있다")
		void updateWishlist_SameName() {
			// Given
			Long wishlistId = 1L;
			String currentName = wishlist.getName();
			WishlistRequest.updateRequest request = new WishlistRequest.updateRequest(currentName);
			Long currentMemberId = 1L;

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));

			// When
			WishlistResponse.UpdateResponse response = wishlistService.updateWishlist(wishlistId, request, currentMemberId);

			// Then
			assertThat(response.id()).isEqualTo(wishlistId);
			assertThat(wishlist.getName()).isEqualTo(currentName);

			verify(wishlistRepository).findById(wishlistId);
		}
	}

	@Nested
	@DisplayName("위시리스트 삭제 테스트")
	class DeleteWishlistTest {

		@Test
		@DisplayName("정상적으로 위시리스트를 삭제한다")
		void deleteWishlist_Success() {
			// Given
			Long wishlistId = 1L;
			Long currentMemberId = 1L;

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));

			// When
			assertThatCode(() -> wishlistService.deleteWishlist(wishlistId, currentMemberId))
				.doesNotThrowAnyException();

			// Then
			verify(wishlistRepository).findById(wishlistId);
			verify(memberRepository).findById(currentMemberId);
			verify(wishlistAccommodationRepository).deleteAllByWishlistId(wishlistId);
			verify(wishlistRepository).delete(wishlist);
		}

		@Test
		@DisplayName("존재하지 않는 위시리스트 삭제 시 예외 발생")
		void deleteWishlist_WishlistNotFound() {
			// Given
			Long nonExistentWishlistId = 999L;
			Long currentMemberId = 1L;

			given(wishlistRepository.findById(nonExistentWishlistId)).willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> wishlistService.deleteWishlist(nonExistentWishlistId, currentMemberId))
				.isInstanceOf(WishlistNotFoundException.class)
				.hasMessage("존재하지 않는 위시리스트입니다.");

			verify(wishlistRepository).findById(nonExistentWishlistId);
			verify(wishlistAccommodationRepository, never()).deleteAllByWishlistId(any());
			verify(wishlistRepository, never()).delete(any());
		}

		@Test
		@DisplayName("존재하지 않는 사용자로 삭제 시 예외 발생")
		void deleteWishlist_MemberNotFound() {
			// Given
			Long wishlistId = 1L;
			Long nonExistentMemberId = 999L;

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(memberRepository.findById(nonExistentMemberId)).willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> wishlistService.deleteWishlist(wishlistId, nonExistentMemberId))
				.isInstanceOf(MemberNotFoundException.class)
				.hasMessage("존재하지 않는 사용자입니다.");

			verify(wishlistRepository).findById(wishlistId);
			verify(memberRepository).findById(nonExistentMemberId);
			verify(wishlistAccommodationRepository, never()).deleteAllByWishlistId(any());
			verify(wishlistRepository, never()).delete(any());
		}

		@Test
		@DisplayName("다른 사용자의 위시리스트 삭제 시 예외 발생")
		void deleteWishlist_AccessDenied() {
			// Given
			Long wishlistId = 2L;
			Long currentMemberId = 1L;

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(otherWishlist));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));

			// When & Then
			assertThatThrownBy(() -> wishlistService.deleteWishlist(wishlistId, currentMemberId))
				.isInstanceOf(WishlistAccessDeniedException.class)
				.hasMessage("위시리스트에 대한 접근 권한이 없습니다.");

			verify(wishlistRepository).findById(wishlistId);
			verify(memberRepository).findById(currentMemberId);
			verify(wishlistAccommodationRepository, never()).deleteAllByWishlistId(any());
			verify(wishlistRepository, never()).delete(any());
		}

		@Test
		@DisplayName("관리자는 다른 사용자의 위시리스트를 삭제할 수 있다")
		void deleteWishlist_AdminCanDelete() {
			// Given
			Long wishlistId = 1L;
			Long adminMemberId = 2L;

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(memberRepository.findById(adminMemberId)).willReturn(Optional.of(adminMember));

			// When
			assertThatCode(() -> wishlistService.deleteWishlist(wishlistId, adminMemberId))
				.doesNotThrowAnyException();

			// Then
			verify(wishlistRepository).findById(wishlistId);
			verify(memberRepository).findById(adminMemberId);
			verify(wishlistAccommodationRepository).deleteAllByWishlistId(wishlistId);
			verify(wishlistRepository).delete(wishlist);
		}
	}

	@Nested
	@DisplayName("위시리스트 목록 조회 테스트")
	class FindWishlistsTest {

		@Test
		@DisplayName("정상적으로 위시리스트 목록을 조회한다")
		void findWishlists_Success() {
			// Given
			Long currentMemberId = 1L;
			CursorRequest.CursorPageRequest request = CursorRequest.CursorPageRequest.builder()
				.size(20)
				.lastId(null)
				.lastCreatedAt(null)
				.build();

			List<Wishlist> wishlists = List.of(
				createWishlistWithId(1L, "서울 여행"),
				createWishlistWithId(2L, "부산 여행"),
				createWishlistWithId(3L, "제주 여행")
			);

			Slice<Wishlist> wishlistSlice = new SliceImpl<>(wishlists, PageRequest.of(0, 20), false);

			Map<Long, Long> wishlistItemCounts = Map.of(
				1L, 3L,
				2L, 5L,
				3L, 2L
			);

			Map<Long, String> thumbnailUrls = Map.of(
				1L, "thumbnail1.jpg",
				2L, "thumbnail2.jpg"
				// 3L은 의도적으로 제외 (썸네일 없음)
			);

			CursorResponse.PageInfo pageInfo = CursorResponse.PageInfo.builder()
				.hasNext(false)
				.nextCursor(null)
				.currentSize(3)
				.build();

			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));
			given(wishlistRepository.findByMemberIdWithCursor(eq(currentMemberId), eq(null), eq(null), any(PageRequest.class)))
				.willReturn(wishlistSlice);
			given(wishlistAccommodationRepository.countByWishlistIds(List.of(1L, 2L, 3L)))
				.willReturn(wishlistItemCounts);
			given(wishlistAccommodationRepository.findLatestThumbnailUrlsByWishlistIds(List.of(1L, 2L, 3L)))
				.willReturn(thumbnailUrls);
			given(cursorPageInfoCreator.createPageInfo(eq(wishlists), eq(false), any(), any()))
				.willReturn(pageInfo);

			// When
			WishlistResponse.WishlistInfos response = wishlistService.findWishlists(request, currentMemberId);

			// Then
			assertThat(response.wishlists()).hasSize(3);
			assertThat(response.pageInfo().hasNext()).isFalse();
			assertThat(response.pageInfo().currentSize()).isEqualTo(3);

			// 첫 번째 위시리스트 검증
			WishlistResponse.WishlistInfo firstWishlist = response.wishlists().getFirst();
			assertThat(firstWishlist.id()).isEqualTo(1L);
			assertThat(firstWishlist.name()).isEqualTo("서울 여행");
			assertThat(firstWishlist.wishlistItemCount()).isEqualTo(3L);
			assertThat(firstWishlist.thumbnailImageUrl()).isEqualTo("thumbnail1.jpg");

			// 세 번째 위시리스트 검증 (썸네일 없음)
			WishlistResponse.WishlistInfo thirdWishlist = response.wishlists().get(2);
			assertThat(thirdWishlist.id()).isEqualTo(3L);
			assertThat(thirdWishlist.thumbnailImageUrl()).isNull();

			verify(memberRepository).findById(currentMemberId);
			verify(wishlistRepository).findByMemberIdWithCursor(eq(currentMemberId), eq(null), eq(null), any(PageRequest.class));
			verify(wishlistAccommodationRepository).countByWishlistIds(List.of(1L, 2L, 3L));
			verify(wishlistAccommodationRepository).findLatestThumbnailUrlsByWishlistIds(List.of(1L, 2L, 3L));
		}

		@Test
		@DisplayName("빈 위시리스트 목록을 조회한다")
		void findWishlists_EmptyList() {
			// Given
			Long currentMemberId = 1L;
			CursorRequest.CursorPageRequest request = CursorRequest.CursorPageRequest.builder()
				.size(20)
				.lastId(null)
				.lastCreatedAt(null)
				.build();

			Slice<Wishlist> emptySlice = new SliceImpl<>(List.of(), PageRequest.of(0, 20), false);

			CursorResponse.PageInfo pageInfo = CursorResponse.PageInfo.builder()
				.hasNext(false)
				.nextCursor(null)
				.currentSize(0)
				.build();

			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));
			given(wishlistRepository.findByMemberIdWithCursor(eq(currentMemberId), eq(null), eq(null), any(PageRequest.class)))
				.willReturn(emptySlice);
			given(wishlistAccommodationRepository.countByWishlistIds(List.of()))
				.willReturn(new HashMap<>());
			given(wishlistAccommodationRepository.findLatestThumbnailUrlsByWishlistIds(List.of()))
				.willReturn(new HashMap<>());
			given(cursorPageInfoCreator.createPageInfo(eq(List.of()), eq(false), any(), any()))
				.willReturn(pageInfo);

			// When
			WishlistResponse.WishlistInfos response = wishlistService.findWishlists(request, currentMemberId);

			// Then
			assertThat(response.wishlists()).isEmpty();
			assertThat(response.pageInfo().hasNext()).isFalse();
			assertThat(response.pageInfo().currentSize()).isZero();

			verify(memberRepository).findById(currentMemberId);
			verify(wishlistRepository).findByMemberIdWithCursor(eq(currentMemberId), eq(null), eq(null), any(PageRequest.class));
		}

		@Test
		@DisplayName("커서 기반 페이징으로 위시리스트 목록을 조회한다")
		void findWishlists_WithCursor() {
			// Given
			Long currentMemberId = 1L;
			Long lastId = 3L;
			LocalDateTime lastCreatedAt = LocalDateTime.now().minusDays(2);

			CursorRequest.CursorPageRequest request = CursorRequest.CursorPageRequest.builder()
				.size(2)
				.lastId(lastId)
				.lastCreatedAt(lastCreatedAt)
				.build();

			List<Wishlist> wishlists = List.of(
				createWishlistWithId(4L, "대구 여행"),
				createWishlistWithId(5L, "광주 여행")
			);

			Slice<Wishlist> wishlistSlice = new SliceImpl<>(wishlists, PageRequest.of(0, 2), true);

			Map<Long, Long> wishlistItemCounts = Map.of(
				4L, 1L,
				5L, 4L
			);

			Map<Long, String> thumbnailUrls = Map.of(
				4L, "thumbnail4.jpg"
				// 5L은 의도적으로 제외 (썸네일 없음)
			);

			CursorResponse.PageInfo pageInfo = CursorResponse.PageInfo.builder()
				.hasNext(true)
				.nextCursor("encoded_cursor")
				.currentSize(2)
				.build();

			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));
			given(wishlistRepository.findByMemberIdWithCursor(eq(currentMemberId), eq(lastId), eq(lastCreatedAt), any(PageRequest.class)))
				.willReturn(wishlistSlice);
			given(wishlistAccommodationRepository.countByWishlistIds(List.of(4L, 5L)))
				.willReturn(wishlistItemCounts);
			given(wishlistAccommodationRepository.findLatestThumbnailUrlsByWishlistIds(List.of(4L, 5L)))
				.willReturn(thumbnailUrls);
			given(cursorPageInfoCreator.createPageInfo(eq(wishlists), eq(true), any(), any()))
				.willReturn(pageInfo);

			// When
			WishlistResponse.WishlistInfos response = wishlistService.findWishlists(request, currentMemberId);

			// Then
			assertThat(response.wishlists()).hasSize(2);
			assertThat(response.pageInfo().hasNext()).isTrue();
			assertThat(response.pageInfo().nextCursor()).isEqualTo("encoded_cursor");
			assertThat(response.pageInfo().currentSize()).isEqualTo(2);

			// 첫 번째 위시리스트 검증
			WishlistResponse.WishlistInfo firstWishlist = response.wishlists().getFirst();
			assertThat(firstWishlist.id()).isEqualTo(4L);
			assertThat(firstWishlist.name()).isEqualTo("대구 여행");
			assertThat(firstWishlist.wishlistItemCount()).isEqualTo(1L);
			assertThat(firstWishlist.thumbnailImageUrl()).isEqualTo("thumbnail4.jpg");

			// 두 번째 위시리스트 검증 (썸네일 없음)
			WishlistResponse.WishlistInfo secondWishlist = response.wishlists().get(1);
			assertThat(secondWishlist.id()).isEqualTo(5L);
			assertThat(secondWishlist.thumbnailImageUrl()).isNull();

			verify(memberRepository).findById(currentMemberId);
			verify(wishlistRepository).findByMemberIdWithCursor(eq(currentMemberId), eq(lastId), eq(lastCreatedAt), any(PageRequest.class));
		}

		@Test
		@DisplayName("존재하지 않는 사용자로 조회 시 예외 발생")
		void findWishlists_MemberNotFound() {
			// Given
			Long nonExistentMemberId = 999L;
			CursorRequest.CursorPageRequest request = CursorRequest.CursorPageRequest.builder()
				.size(20)
				.lastId(null)
				.lastCreatedAt(null)
				.build();

			given(memberRepository.findById(nonExistentMemberId)).willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> wishlistService.findWishlists(request, nonExistentMemberId))
				.isInstanceOf(MemberNotFoundException.class)
				.hasMessage("존재하지 않는 사용자입니다.");

			verify(memberRepository).findById(nonExistentMemberId);
			verify(wishlistRepository, never()).findByMemberIdWithCursor(anyLong(), any(), any(), any(PageRequest.class));
		}

		@Test
		@DisplayName("사용자별로 위시리스트가 분리되어 조회된다")
		void findWishlists_IsolatedByMember() {
			// Given
			Long currentMemberId = 1L;
			CursorRequest.CursorPageRequest request = CursorRequest.CursorPageRequest.builder()
				.size(20)
				.lastId(null)
				.lastCreatedAt(null)
				.build();

			List<Wishlist> memberWishlists = List.of(
				createWishlistWithId(1L, "내 서울 여행")
			);

			Slice<Wishlist> wishlistSlice = new SliceImpl<>(memberWishlists, PageRequest.of(0, 20), false);

			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));
			given(wishlistRepository.findByMemberIdWithCursor(eq(currentMemberId), isNull(), isNull(), any(PageRequest.class)))
				.willReturn(wishlistSlice);
			given(wishlistAccommodationRepository.countByWishlistIds(List.of(1L)))
				.willReturn(Map.of(1L, 0L));
			given(wishlistAccommodationRepository.findLatestThumbnailUrlsByWishlistIds(List.of(1L)))
				.willReturn(Map.of());
			given(cursorPageInfoCreator.createPageInfo(eq(memberWishlists), eq(false), any(), any()))
				.willReturn(CursorResponse.PageInfo.builder()
					.hasNext(false)
					.nextCursor(null)
					.currentSize(1)
					.build());

			// When
			WishlistResponse.WishlistInfos response = wishlistService.findWishlists(request, currentMemberId);

			// Then
			assertThat(response.wishlists()).hasSize(1);
			assertThat(response.wishlists().getFirst().name()).isEqualTo("내 서울 여행");

			// 해당 사용자의 위시리스트만 조회되는지 확인
			verify(wishlistRepository).findByMemberIdWithCursor(eq(currentMemberId), isNull(), isNull(), any(PageRequest.class));
		}
	}

	// 테스트 헬퍼 메서드
	private Wishlist createWishlistWithId(Long id, String name) {
		return Wishlist.builder()
			.id(id)
			.name(name)
			.member(member)
			.build();
	}

	@Nested
	@DisplayName("위시리스트 숙소 추가 테스트")
	class CreateWishlistAccommodationTest {

		private Accommodation accommodation;

		@BeforeEach
		void setUpAccommodation() {
			accommodation = Accommodation.builder()
				.id(100L)
				.name("신라호텔")
				.build();
		}

		@Test
		@DisplayName("정상적으로 위시리스트에 숙소를 추가한다")
		void createWishlistAccommodation_Success() {
			// Given
			Long wishlistId = 1L;
			Long accommodationId = 100L;
			Long currentMemberId = 1L;
			WishlistRequest.CreateWishlistAccommodationRequest request =
				new WishlistRequest.CreateWishlistAccommodationRequest(accommodationId);

			// Mock 설정
			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(accommodationRepository.findById(accommodationId)).willReturn(Optional.of(accommodation));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));
			given(wishlistAccommodationRepository.existsByWishlistIdAndAccommodationId(wishlistId, accommodationId))
				.willReturn(false);

			WishlistAccommodation savedWishlistAccommodation = WishlistAccommodation.builder()
				.id(10L)
				.wishlist(wishlist)
				.accommodation(accommodation)
				.build();

			given(wishlistAccommodationRepository.save(any(WishlistAccommodation.class)))
				.willReturn(savedWishlistAccommodation);

			// When
			WishlistResponse.CreateWishlistAccommodationResponse response =
				wishlistService.createWishlistAccommodation(wishlistId, request, currentMemberId);

			// Then
			assertThat(response.id()).isEqualTo(10L);

			verify(wishlistRepository).findById(wishlistId);
			verify(accommodationRepository).findById(accommodationId);
			verify(memberRepository).findById(currentMemberId);
			verify(wishlistAccommodationRepository).existsByWishlistIdAndAccommodationId(wishlistId, accommodationId);
			verify(wishlistAccommodationRepository).save(any(WishlistAccommodation.class));
		}

		@Test
		@DisplayName("존재하지 않는 위시리스트에 숙소 추가 시 예외 발생")
		void createWishlistAccommodation_WishlistNotFound() {
			// Given
			Long nonExistentWishlistId = 999L;
			Long accommodationId = 100L;
			Long currentMemberId = 1L;
			WishlistRequest.CreateWishlistAccommodationRequest request =
				new WishlistRequest.CreateWishlistAccommodationRequest(accommodationId);

			given(wishlistRepository.findById(nonExistentWishlistId)).willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> wishlistService.createWishlistAccommodation(nonExistentWishlistId, request, currentMemberId))
				.isInstanceOf(WishlistNotFoundException.class)
				.hasMessage("존재하지 않는 위시리스트입니다.");

			verify(wishlistRepository).findById(nonExistentWishlistId);
			verify(accommodationRepository, never()).findById(any());
			verify(memberRepository, never()).findById(any());
			verify(wishlistAccommodationRepository, never()).save(any());
		}

		@Test
		@DisplayName("존재하지 않는 숙소를 위시리스트에 추가 시 예외 발생")
		void createWishlistAccommodation_AccommodationNotFound() {
			// Given
			Long wishlistId = 1L;
			Long nonExistentAccommodationId = 999L;
			Long currentMemberId = 1L;
			WishlistRequest.CreateWishlistAccommodationRequest request =
				new WishlistRequest.CreateWishlistAccommodationRequest(nonExistentAccommodationId);

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(accommodationRepository.findById(nonExistentAccommodationId)).willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> wishlistService.createWishlistAccommodation(wishlistId, request, currentMemberId))
				.isInstanceOf(AccommodationNotFoundException.class)
				.hasMessage("존재하지 않는 숙소입니다.");

			verify(wishlistRepository).findById(wishlistId);
			verify(accommodationRepository).findById(nonExistentAccommodationId);
			verify(memberRepository, never()).findById(any());
			verify(wishlistAccommodationRepository, never()).save(any());
		}

		@Test
		@DisplayName("존재하지 않는 사용자로 숙소 추가 시 예외 발생")
		void createWishlistAccommodation_MemberNotFound() {
			// Given
			Long wishlistId = 1L;
			Long accommodationId = 100L;
			Long nonExistentMemberId = 999L;
			WishlistRequest.CreateWishlistAccommodationRequest request =
				new WishlistRequest.CreateWishlistAccommodationRequest(accommodationId);

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(accommodationRepository.findById(accommodationId)).willReturn(Optional.of(accommodation));
			given(memberRepository.findById(nonExistentMemberId)).willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> wishlistService.createWishlistAccommodation(wishlistId, request, nonExistentMemberId))
				.isInstanceOf(MemberNotFoundException.class)
				.hasMessage("존재하지 않는 사용자입니다.");

			verify(wishlistRepository).findById(wishlistId);
			verify(accommodationRepository).findById(accommodationId);
			verify(memberRepository).findById(nonExistentMemberId);
			verify(wishlistAccommodationRepository, never()).save(any());
		}

		@Test
		@DisplayName("다른 사용자의 위시리스트에 숙소 추가 시 예외 발생")
		void createWishlistAccommodation_AccessDenied() {
			// Given
			Long otherWishlistId = 2L;
			Long accommodationId = 100L;
			Long currentMemberId = 1L; // 다른 사용자 ID
			WishlistRequest.CreateWishlistAccommodationRequest request =
				new WishlistRequest.CreateWishlistAccommodationRequest(accommodationId);

			given(wishlistRepository.findById(otherWishlistId)).willReturn(Optional.of(otherWishlist));
			given(accommodationRepository.findById(accommodationId)).willReturn(Optional.of(accommodation));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));

			// When & Then
			assertThatThrownBy(() -> wishlistService.createWishlistAccommodation(otherWishlistId, request, currentMemberId))
				.isInstanceOf(WishlistAccessDeniedException.class)
				.hasMessage("위시리스트에 대한 접근 권한이 없습니다.");

			verify(wishlistRepository).findById(otherWishlistId);
			verify(accommodationRepository).findById(accommodationId);
			verify(memberRepository).findById(currentMemberId);
			verify(wishlistAccommodationRepository, never()).save(any());
		}

		@Test
		@DisplayName("이미 위시리스트에 있는 숙소를 중복 추가 시 예외 발생")
		void createWishlistAccommodation_DuplicateAccommodation() {
			// Given
			Long wishlistId = 1L;
			Long accommodationId = 100L;
			Long currentMemberId = 1L;
			WishlistRequest.CreateWishlistAccommodationRequest request =
				new WishlistRequest.CreateWishlistAccommodationRequest(accommodationId);

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(accommodationRepository.findById(accommodationId)).willReturn(Optional.of(accommodation));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));
			given(wishlistAccommodationRepository.existsByWishlistIdAndAccommodationId(wishlistId, accommodationId))
				.willReturn(true); // 이미 존재함

			// When & Then
			assertThatThrownBy(() -> wishlistService.createWishlistAccommodation(wishlistId, request, currentMemberId))
				.isInstanceOf(WishlistAccessDeniedException.class)
				.hasMessage("위시리스트에 대한 접근 권한이 없습니다.");

			verify(wishlistRepository).findById(wishlistId);
			verify(accommodationRepository).findById(accommodationId);
			verify(memberRepository).findById(currentMemberId);
			verify(wishlistAccommodationRepository).existsByWishlistIdAndAccommodationId(wishlistId, accommodationId);
			verify(wishlistAccommodationRepository, never()).save(any());
		}

		@Test
		@DisplayName("같은 숙소를 여러 위시리스트에 추가할 수 있다")
		void createWishlistAccommodation_SameAccommodationDifferentWishlists() {
			// Given
			Long accommodationId = 100L;
			Long currentMemberId = 1L;
			WishlistRequest.CreateWishlistAccommodationRequest request =
				new WishlistRequest.CreateWishlistAccommodationRequest(accommodationId);

			// 첫 번째 위시리스트에 추가
			Long firstWishlistId = 1L;
			Wishlist firstWishlist = Wishlist.builder()
				.id(firstWishlistId)
				.name("첫 번째 위시리스트")
				.member(member)
				.build();

			// 두 번째 위시리스트에 추가
			Long secondWishlistId = 2L;
			Wishlist secondWishlist = Wishlist.builder()
				.id(secondWishlistId)
				.name("두 번째 위시리스트")
				.member(member)
				.build();

			// 첫 번째 위시리스트 추가
			given(wishlistRepository.findById(firstWishlistId)).willReturn(Optional.of(firstWishlist));
			given(accommodationRepository.findById(accommodationId)).willReturn(Optional.of(accommodation));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));
			given(wishlistAccommodationRepository.existsByWishlistIdAndAccommodationId(firstWishlistId, accommodationId))
				.willReturn(false);

			WishlistAccommodation firstSavedWishlistAccommodation = WishlistAccommodation.builder()
				.id(10L)
				.wishlist(firstWishlist)
				.accommodation(accommodation)
				.build();

			given(wishlistAccommodationRepository.save(any(WishlistAccommodation.class)))
				.willReturn(firstSavedWishlistAccommodation);

			// When
			WishlistResponse.CreateWishlistAccommodationResponse firstResponse =
				wishlistService.createWishlistAccommodation(firstWishlistId, request, currentMemberId);

			// 두 번째 위시리스트 추가
			given(wishlistRepository.findById(secondWishlistId)).willReturn(Optional.of(secondWishlist));
			given(wishlistAccommodationRepository.existsByWishlistIdAndAccommodationId(secondWishlistId, accommodationId))
				.willReturn(false);

			WishlistAccommodation secondSavedWishlistAccommodation = WishlistAccommodation.builder()
				.id(20L)
				.wishlist(secondWishlist)
				.accommodation(accommodation)
				.build();

			given(wishlistAccommodationRepository.save(any(WishlistAccommodation.class)))
				.willReturn(secondSavedWishlistAccommodation);

			WishlistResponse.CreateWishlistAccommodationResponse secondResponse =
				wishlistService.createWishlistAccommodation(secondWishlistId, request, currentMemberId);

			// Then
			assertThat(firstResponse.id()).isEqualTo(10L);
			assertThat(secondResponse.id()).isEqualTo(20L);

			verify(wishlistRepository).findById(firstWishlistId);
			verify(wishlistRepository).findById(secondWishlistId);
			verify(accommodationRepository, times(2)).findById(accommodationId);
			verify(memberRepository, times(2)).findById(currentMemberId);
			verify(wishlistAccommodationRepository).existsByWishlistIdAndAccommodationId(firstWishlistId, accommodationId);
			verify(wishlistAccommodationRepository).existsByWishlistIdAndAccommodationId(secondWishlistId, accommodationId);
			verify(wishlistAccommodationRepository, times(2)).save(any(WishlistAccommodation.class));
		}

		@Test
		@DisplayName("여러 숙소를 같은 위시리스트에 순차적으로 추가할 수 있다")
		void createWishlistAccommodation_MultipleAccommodationsToSameWishlist() {
			// Given
			Long wishlistId = 1L;
			Long currentMemberId = 1L;
			Long[] accommodationIds = {100L, 200L, 300L};

			for (int i = 0; i < accommodationIds.length; i++) {
				Long accommodationId = accommodationIds[i];
				WishlistRequest.CreateWishlistAccommodationRequest request =
					new WishlistRequest.CreateWishlistAccommodationRequest(accommodationId);

				Accommodation accommodation = Accommodation.builder()
					.id(accommodationId)
					.name("숙소 " + (i + 1))
					.build();

				given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
				given(accommodationRepository.findById(accommodationId)).willReturn(Optional.of(accommodation));
				given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));
				given(wishlistAccommodationRepository.existsByWishlistIdAndAccommodationId(wishlistId, accommodationId))
					.willReturn(false);

				WishlistAccommodation savedWishlistAccommodation = WishlistAccommodation.builder()
					.id((long) (i + 10))
					.wishlist(wishlist)
					.accommodation(accommodation)
					.build();

				given(wishlistAccommodationRepository.save(any(WishlistAccommodation.class)))
					.willReturn(savedWishlistAccommodation);

				// When
				WishlistResponse.CreateWishlistAccommodationResponse response =
					wishlistService.createWishlistAccommodation(wishlistId, request, currentMemberId);

				// Then
				assertThat(response.id()).isEqualTo(i + 10);
			}

			verify(wishlistRepository, times(3)).findById(wishlistId);
			verify(accommodationRepository).findById(100L);
			verify(accommodationRepository).findById(200L);
			verify(accommodationRepository).findById(300L);
			verify(memberRepository, times(3)).findById(currentMemberId);
			verify(wishlistAccommodationRepository, times(3)).save(any(WishlistAccommodation.class));
		}

		@Test
		@DisplayName("위시리스트 항목 저장 시 올바른 데이터로 생성된다")
		void createWishlistAccommodation_CorrectDataSaved() {
			// Given
			Long wishlistId = 1L;
			Long accommodationId = 100L;
			Long currentMemberId = 1L;
			WishlistRequest.CreateWishlistAccommodationRequest request =
				new WishlistRequest.CreateWishlistAccommodationRequest(accommodationId);

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(accommodationRepository.findById(accommodationId)).willReturn(Optional.of(accommodation));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));
			given(wishlistAccommodationRepository.existsByWishlistIdAndAccommodationId(wishlistId, accommodationId))
				.willReturn(false);

			WishlistAccommodation savedWishlistAccommodation = WishlistAccommodation.builder()
				.id(10L)
				.wishlist(wishlist)
				.accommodation(accommodation)
				.build();

			given(wishlistAccommodationRepository.save(any(WishlistAccommodation.class)))
				.willReturn(savedWishlistAccommodation);

			// When
			wishlistService.createWishlistAccommodation(wishlistId, request, currentMemberId);

			// Then - ArgumentCaptor를 사용하여 저장된 데이터 검증
			ArgumentCaptor<WishlistAccommodation> captor = ArgumentCaptor.forClass(WishlistAccommodation.class);
			verify(wishlistAccommodationRepository).save(captor.capture());

			WishlistAccommodation capturedWishlistAccommodation = captor.getValue();
			assertThat(capturedWishlistAccommodation.getWishlist()).isEqualTo(wishlist);
			assertThat(capturedWishlistAccommodation.getAccommodation()).isEqualTo(accommodation);
			assertThat(capturedWishlistAccommodation.getMemo()).isNull(); // 기본값은 null
		}
	}

	@Nested
	@DisplayName("위시리스트 숙소 메모 수정 테스트")
	class UpdateWishlistAccommodationTest {

		private Accommodation accommodation;
		private WishlistAccommodation wishlistAccommodation;
		private WishlistAccommodation otherWishlistAccommodation;

		@BeforeEach
		void setUpWishlistAccommodation() {

			accommodation = Accommodation.builder()
				.id(100L)
				.name("신라호텔")
				.build();

			// 현재 사용자의 위시리스트에 속한 숙소 항목
			wishlistAccommodation = WishlistAccommodation.builder()
				.id(10L)
				.memo("기존 메모")
				.wishlist(wishlist)
				.accommodation(accommodation)
				.build();

			// 다른 위시리스트에 속한 숙소 항목
			otherWishlistAccommodation = WishlistAccommodation.builder()
				.id(20L)
				.memo("다른 위시리스트 메모")
				.wishlist(otherWishlist)
				.accommodation(accommodation)
				.build();

		}

		@Test
		@DisplayName("정상적으로 위시리스트 숙소 메모를 수정한다")
		void updateWishlistAccommodation_Success() {
			// Given
			Long wishlistId = 1L;
			Long wishlistAccommodationId = 10L;
			Long currentMemberId = 1L;
			String newMemo = "수정된 메모입니다. 정말 좋은 곳이에요!";
			WishlistRequest.UpdateWishlistAccommodationRequest request =
				new WishlistRequest.UpdateWishlistAccommodationRequest(newMemo);

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(wishlistAccommodationRepository.findById(wishlistAccommodationId))
				.willReturn(Optional.of(wishlistAccommodation));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));

			// When
			WishlistResponse.UpdateWishlistAccommodationResponse response =
				wishlistService.updateWishlistAccommodation(wishlistId, wishlistAccommodationId, request, currentMemberId);

			// Then
			assertThat(response.id()).isEqualTo(wishlistAccommodationId);
			assertThat(wishlistAccommodation.getMemo()).isEqualTo(newMemo);

			verify(wishlistRepository).findById(wishlistId);
			verify(wishlistAccommodationRepository).findById(wishlistAccommodationId);
			verify(memberRepository).findById(currentMemberId);
		}

		@Test
		@DisplayName("존재하지 않는 위시리스트의 숙소 메모 수정 시 예외 발생")
		void updateWishlistAccommodation_WishlistNotFound() {
			// Given
			Long nonExistentWishlistId = 999L;
			Long wishlistAccommodationId = 10L;
			Long currentMemberId = 1L;
			WishlistRequest.UpdateWishlistAccommodationRequest request =
				new WishlistRequest.UpdateWishlistAccommodationRequest("수정된 메모");

			given(wishlistRepository.findById(nonExistentWishlistId)).willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> wishlistService.updateWishlistAccommodation(
				nonExistentWishlistId, wishlistAccommodationId, request, currentMemberId))
				.isInstanceOf(WishlistNotFoundException.class)
				.hasMessage("존재하지 않는 위시리스트입니다.");

			verify(wishlistRepository).findById(nonExistentWishlistId);
			verify(wishlistAccommodationRepository, never()).findById(any());
			verify(memberRepository, never()).findById(any());
		}

		@Test
		@DisplayName("존재하지 않는 위시리스트 항목 메모 수정 시 예외 발생")
		void updateWishlistAccommodation_WishlistAccommodationNotFound() {
			// Given
			Long wishlistId = 1L;
			Long nonExistentWishlistAccommodationId = 999L;
			Long currentMemberId = 1L;
			WishlistRequest.UpdateWishlistAccommodationRequest request =
				new WishlistRequest.UpdateWishlistAccommodationRequest("수정된 메모");

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(wishlistAccommodationRepository.findById(nonExistentWishlistAccommodationId))
				.willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> wishlistService.updateWishlistAccommodation(
				wishlistId, nonExistentWishlistAccommodationId, request, currentMemberId))
				.isInstanceOf(WishlistAccommodationNotFoundException.class)
				.hasMessage("존재하지 않는 위시리스트 항목입니다.");

			verify(wishlistRepository).findById(wishlistId);
			verify(wishlistAccommodationRepository).findById(nonExistentWishlistAccommodationId);
			verify(memberRepository, never()).findById(any());
		}

		@Test
		@DisplayName("존재하지 않는 사용자로 메모 수정 시 예외 발생")
		void updateWishlistAccommodation_MemberNotFound() {
			// Given
			Long wishlistId = 1L;
			Long wishlistAccommodationId = 10L;
			Long nonExistentMemberId = 999L;
			WishlistRequest.UpdateWishlistAccommodationRequest request =
				new WishlistRequest.UpdateWishlistAccommodationRequest("수정된 메모");

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(wishlistAccommodationRepository.findById(wishlistAccommodationId))
				.willReturn(Optional.of(wishlistAccommodation));
			given(memberRepository.findById(nonExistentMemberId)).willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> wishlistService.updateWishlistAccommodation(
				wishlistId, wishlistAccommodationId, request, nonExistentMemberId))
				.isInstanceOf(MemberNotFoundException.class)
				.hasMessage("존재하지 않는 사용자입니다.");

			verify(wishlistRepository).findById(wishlistId);
			verify(wishlistAccommodationRepository).findById(wishlistAccommodationId);
			verify(memberRepository).findById(nonExistentMemberId);
		}

		@Test
		@DisplayName("다른 사용자의 위시리스트 숙소 메모 수정 시 예외 발생")
		void updateWishlistAccommodation_WishlistAccessDenied() {
			// Given
			Long otherWishlistId = 2L;
			Long wishlistAccommodationId = 10L;
			Long currentMemberId = 1L; // 다른 사용자 ID
			WishlistRequest.UpdateWishlistAccommodationRequest request =
				new WishlistRequest.UpdateWishlistAccommodationRequest("수정된 메모");

			given(wishlistRepository.findById(otherWishlistId)).willReturn(Optional.of(otherWishlist));
			given(wishlistAccommodationRepository.findById(wishlistAccommodationId))
				.willReturn(Optional.of(wishlistAccommodation));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));

			// When & Then
			assertThatThrownBy(() -> wishlistService.updateWishlistAccommodation(
				otherWishlistId, wishlistAccommodationId, request, currentMemberId))
				.isInstanceOf(WishlistAccessDeniedException.class)
				.hasMessage("위시리스트에 대한 접근 권한이 없습니다.");

			verify(wishlistRepository).findById(otherWishlistId);
			verify(wishlistAccommodationRepository).findById(wishlistAccommodationId);
			verify(memberRepository).findById(currentMemberId);
		}

		@Test
		@DisplayName("다른 위시리스트에 속한 항목의 메모 수정 시 예외 발생")
		void updateWishlistAccommodation_WishlistAccommodationAccessDenied() {
			// Given
			Long wishlistId = 1L;
			Long otherWishlistAccommodationId = 20L; // 다른 위시리스트에 속한 항목
			Long currentMemberId = 1L;
			WishlistRequest.UpdateWishlistAccommodationRequest request =
				new WishlistRequest.UpdateWishlistAccommodationRequest("수정된 메모");

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(wishlistAccommodationRepository.findById(otherWishlistAccommodationId))
				.willReturn(Optional.of(otherWishlistAccommodation));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));

			// When & Then
			assertThatThrownBy(() -> wishlistService.updateWishlistAccommodation(
				wishlistId, otherWishlistAccommodationId, request, currentMemberId))
				.isInstanceOf(WishlistAccommodationAccessDeniedException.class)
				.hasMessage("위시리스트 항목에 대한 접근 권한이 없습니다.");

			verify(wishlistRepository).findById(wishlistId);
			verify(wishlistAccommodationRepository).findById(otherWishlistAccommodationId);
			verify(memberRepository).findById(currentMemberId);
		}

		@Test
		@DisplayName("빈 메모로 수정할 수 있다")
		void updateWishlistAccommodation_EmptyMemo() {
			// Given
			Long wishlistId = 1L;
			Long wishlistAccommodationId = 10L;
			Long currentMemberId = 1L;
			String emptyMemo = "";
			WishlistRequest.UpdateWishlistAccommodationRequest request =
				new WishlistRequest.UpdateWishlistAccommodationRequest(emptyMemo);

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(wishlistAccommodationRepository.findById(wishlistAccommodationId))
				.willReturn(Optional.of(wishlistAccommodation));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));

			// When
			WishlistResponse.UpdateWishlistAccommodationResponse response =
				wishlistService.updateWishlistAccommodation(wishlistId, wishlistAccommodationId, request, currentMemberId);

			// Then
			assertThat(response.id()).isEqualTo(wishlistAccommodationId);
			assertThat(wishlistAccommodation.getMemo()).isEqualTo(emptyMemo);

			verify(wishlistRepository).findById(wishlistId);
			verify(wishlistAccommodationRepository).findById(wishlistAccommodationId);
			verify(memberRepository).findById(currentMemberId);
		}

		@Test
		@DisplayName("최대 길이(1024자)의 메모로 수정할 수 있다")
		void updateWishlistAccommodation_MaxLengthMemo() {
			// Given
			Long wishlistId = 1L;
			Long wishlistAccommodationId = 10L;
			Long currentMemberId = 1L;
			String maxLengthMemo = "A".repeat(1024);
			WishlistRequest.UpdateWishlistAccommodationRequest request =
				new WishlistRequest.UpdateWishlistAccommodationRequest(maxLengthMemo);

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(wishlistAccommodationRepository.findById(wishlistAccommodationId))
				.willReturn(Optional.of(wishlistAccommodation));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));

			// When
			WishlistResponse.UpdateWishlistAccommodationResponse response =
				wishlistService.updateWishlistAccommodation(wishlistId, wishlistAccommodationId, request, currentMemberId);

			// Then
			assertThat(response.id()).isEqualTo(wishlistAccommodationId);
			assertThat(wishlistAccommodation.getMemo()).isEqualTo(maxLengthMemo);
			assertThat(wishlistAccommodation.getMemo().length()).isEqualTo(1024);

			verify(wishlistRepository).findById(wishlistId);
			verify(wishlistAccommodationRepository).findById(wishlistAccommodationId);
			verify(memberRepository).findById(currentMemberId);
		}

		@Test
		@DisplayName("특수 문자가 포함된 메모로 수정할 수 있다")
		void updateWishlistAccommodation_SpecialCharacterMemo() {
			// Given
			Long wishlistId = 1L;
			Long wishlistAccommodationId = 10L;
			Long currentMemberId = 1L;
			String specialCharacterMemo = "정말 좋은 곳! 🏨✨ 가격도 합리적 (★★★★★) 직원분들도 친절 😊 #추천 @여행";
			WishlistRequest.UpdateWishlistAccommodationRequest request =
				new WishlistRequest.UpdateWishlistAccommodationRequest(specialCharacterMemo);

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(wishlistAccommodationRepository.findById(wishlistAccommodationId))
				.willReturn(Optional.of(wishlistAccommodation));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));

			// When
			WishlistResponse.UpdateWishlistAccommodationResponse response =
				wishlistService.updateWishlistAccommodation(wishlistId, wishlistAccommodationId, request, currentMemberId);

			// Then
			assertThat(response.id()).isEqualTo(wishlistAccommodationId);
			assertThat(wishlistAccommodation.getMemo()).isEqualTo(specialCharacterMemo);

			verify(wishlistRepository).findById(wishlistId);
			verify(wishlistAccommodationRepository).findById(wishlistAccommodationId);
			verify(memberRepository).findById(currentMemberId);
		}

		@Test
		@DisplayName("같은 내용으로 메모를 수정할 수 있다")
		void updateWishlistAccommodation_SameMemo() {
			// Given
			Long wishlistId = 1L;
			Long wishlistAccommodationId = 10L;
			Long currentMemberId = 1L;
			String currentMemo = wishlistAccommodation.getMemo(); // "기존 메모"
			WishlistRequest.UpdateWishlistAccommodationRequest request =
				new WishlistRequest.UpdateWishlistAccommodationRequest(currentMemo);

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(wishlistAccommodationRepository.findById(wishlistAccommodationId))
				.willReturn(Optional.of(wishlistAccommodation));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));

			// When
			WishlistResponse.UpdateWishlistAccommodationResponse response =
				wishlistService.updateWishlistAccommodation(wishlistId, wishlistAccommodationId, request, currentMemberId);

			// Then
			assertThat(response.id()).isEqualTo(wishlistAccommodationId);
			assertThat(wishlistAccommodation.getMemo()).isEqualTo(currentMemo);

			verify(wishlistRepository).findById(wishlistId);
			verify(wishlistAccommodationRepository).findById(wishlistAccommodationId);
			verify(memberRepository).findById(currentMemberId);
		}

		@Test
		@DisplayName("여러 위시리스트 항목의 메모를 순차적으로 수정할 수 있다")
		void updateWishlistAccommodation_MultipleItemsSequentially() {
			// Given
			Long wishlistId = 1L;
			Long currentMemberId = 1L;
			Long[] wishlistAccommodationIds = {10L, 11L, 12L};
			String[] memos = {"첫 번째 메모", "두 번째 메모", "세 번째 메모"};

			for (int i = 0; i < wishlistAccommodationIds.length; i++) {
				Long wishlistAccommodationId = wishlistAccommodationIds[i];
				String memo = memos[i];

				WishlistAccommodation accommodation = WishlistAccommodation.builder()
					.id(wishlistAccommodationId)
					.memo("기존 메모 " + (i + 1))
					.wishlist(wishlist)
					.accommodation(this.accommodation)
					.build();

				WishlistRequest.UpdateWishlistAccommodationRequest request =
					new WishlistRequest.UpdateWishlistAccommodationRequest(memo);

				given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
				given(wishlistAccommodationRepository.findById(wishlistAccommodationId))
					.willReturn(Optional.of(accommodation));
				given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));

				// When
				WishlistResponse.UpdateWishlistAccommodationResponse response =
					wishlistService.updateWishlistAccommodation(wishlistId, wishlistAccommodationId, request, currentMemberId);

				// Then
				assertThat(response.id()).isEqualTo(wishlistAccommodationId);
				assertThat(accommodation.getMemo()).isEqualTo(memo);
			}

			verify(wishlistRepository, times(3)).findById(wishlistId);
			verify(wishlistAccommodationRepository).findById(10L);
			verify(wishlistAccommodationRepository).findById(11L);
			verify(wishlistAccommodationRepository).findById(12L);
			verify(memberRepository, times(3)).findById(currentMemberId);
		}

		@Test
		@DisplayName("메모 수정 시 다른 필드는 변경되지 않는다")
		void updateWishlistAccommodation_OnlyMemoChanged() {
			// Given
			Long wishlistId = 1L;
			Long wishlistAccommodationId = 10L;
			Long currentMemberId = 1L;
			String newMemo = "새로운 메모";
			WishlistRequest.UpdateWishlistAccommodationRequest request =
				new WishlistRequest.UpdateWishlistAccommodationRequest(newMemo);

			// 수정 전 상태 저장
			Wishlist originalWishlist = wishlistAccommodation.getWishlist();
			Accommodation originalAccommodation = wishlistAccommodation.getAccommodation();
			Long originalId = wishlistAccommodation.getId();

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(wishlistAccommodationRepository.findById(wishlistAccommodationId))
				.willReturn(Optional.of(wishlistAccommodation));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));

			// When
			wishlistService.updateWishlistAccommodation(wishlistId, wishlistAccommodationId, request, currentMemberId);

			// Then
			assertThat(wishlistAccommodation.getMemo()).isEqualTo(newMemo);
			// 다른 필드들은 변경되지 않아야 함
			assertThat(wishlistAccommodation.getId()).isEqualTo(originalId);
			assertThat(wishlistAccommodation.getWishlist()).isEqualTo(originalWishlist);
			assertThat(wishlistAccommodation.getAccommodation()).isEqualTo(originalAccommodation);

			verify(wishlistRepository).findById(wishlistId);
			verify(wishlistAccommodationRepository).findById(wishlistAccommodationId);
			verify(memberRepository).findById(currentMemberId);
		}
	}

	@Nested
	@DisplayName("위시리스트 숙소 삭제 테스트")
	class DeleteWishlistAccommodationTest {

		private Accommodation accommodation;
		private WishlistAccommodation wishlistAccommodation;
		private WishlistAccommodation otherWishlistAccommodation;

		@BeforeEach
		void setUpWishlistAccommodation() {

			accommodation = Accommodation.builder()
				.id(100L)
				.name("신라호텔")
				.build();

			// 현재 사용자의 위시리스트에 속한 숙소 항목
			wishlistAccommodation = WishlistAccommodation.builder()
				.id(10L)
				.memo("삭제할 메모")
				.wishlist(wishlist)
				.accommodation(accommodation)
				.build();

			// 다른 위시리스트에 속한 숙소 항목
			otherWishlistAccommodation = WishlistAccommodation.builder()
				.id(20L)
				.memo("다른 위시리스트 메모")
				.wishlist(otherWishlist)
				.accommodation(accommodation)
				.build();
		}

		@Test
		@DisplayName("정상적으로 위시리스트에서 숙소를 삭제한다")
		void deleteWishlistAccommodation_Success() {
			// Given
			Long wishlistId = 1L;
			Long wishlistAccommodationId = 10L;
			Long currentMemberId = 1L;

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(wishlistAccommodationRepository.findById(wishlistAccommodationId))
				.willReturn(Optional.of(wishlistAccommodation));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));

			// When
			assertThatCode(() -> wishlistService.deleteWishlistAccommodation(
				wishlistId, wishlistAccommodationId, currentMemberId))
				.doesNotThrowAnyException();

			// Then
			verify(wishlistRepository).findById(wishlistId);
			verify(wishlistAccommodationRepository).findById(wishlistAccommodationId);
			verify(memberRepository).findById(currentMemberId);
			verify(wishlistAccommodationRepository).delete(wishlistAccommodation);
		}

		@Test
		@DisplayName("존재하지 않는 위시리스트의 숙소 삭제 시 예외 발생")
		void deleteWishlistAccommodation_WishlistNotFound() {
			// Given
			Long nonExistentWishlistId = 999L;
			Long wishlistAccommodationId = 10L;
			Long currentMemberId = 1L;

			given(wishlistRepository.findById(nonExistentWishlistId)).willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> wishlistService.deleteWishlistAccommodation(
				nonExistentWishlistId, wishlistAccommodationId, currentMemberId))
				.isInstanceOf(WishlistNotFoundException.class)
				.hasMessage("존재하지 않는 위시리스트입니다.");

			verify(wishlistRepository).findById(nonExistentWishlistId);
			verify(wishlistAccommodationRepository, never()).findById(any());
			verify(memberRepository, never()).findById(any());
			verify(wishlistAccommodationRepository, never()).delete(any());
		}

		@Test
		@DisplayName("존재하지 않는 위시리스트 항목 삭제 시 예외 발생")
		void deleteWishlistAccommodation_WishlistAccommodationNotFound() {
			// Given
			Long wishlistId = 1L;
			Long nonExistentWishlistAccommodationId = 999L;
			Long currentMemberId = 1L;

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(wishlistAccommodationRepository.findById(nonExistentWishlistAccommodationId))
				.willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> wishlistService.deleteWishlistAccommodation(
				wishlistId, nonExistentWishlistAccommodationId, currentMemberId))
				.isInstanceOf(WishlistAccommodationNotFoundException.class)
				.hasMessage("존재하지 않는 위시리스트 항목입니다.");

			verify(wishlistRepository).findById(wishlistId);
			verify(wishlistAccommodationRepository).findById(nonExistentWishlistAccommodationId);
			verify(memberRepository, never()).findById(any());
			verify(wishlistAccommodationRepository, never()).delete(any());
		}

		@Test
		@DisplayName("존재하지 않는 사용자로 삭제 시 예외 발생")
		void deleteWishlistAccommodation_MemberNotFound() {
			// Given
			Long wishlistId = 1L;
			Long wishlistAccommodationId = 10L;
			Long nonExistentMemberId = 999L;

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(wishlistAccommodationRepository.findById(wishlistAccommodationId))
				.willReturn(Optional.of(wishlistAccommodation));
			given(memberRepository.findById(nonExistentMemberId)).willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> wishlistService.deleteWishlistAccommodation(
				wishlistId, wishlistAccommodationId, nonExistentMemberId))
				.isInstanceOf(MemberNotFoundException.class)
				.hasMessage("존재하지 않는 사용자입니다.");

			verify(wishlistRepository).findById(wishlistId);
			verify(wishlistAccommodationRepository).findById(wishlistAccommodationId);
			verify(memberRepository).findById(nonExistentMemberId);
			verify(wishlistAccommodationRepository, never()).delete(any());
		}

		@Test
		@DisplayName("다른 사용자의 위시리스트 숙소 삭제 시 예외 발생")
		void deleteWishlistAccommodation_WishlistAccessDenied() {
			// Given
			Long otherWishlistId = 2L;
			Long wishlistAccommodationId = 10L;
			Long currentMemberId = 1L; // 다른 사용자 ID

			given(wishlistRepository.findById(otherWishlistId)).willReturn(Optional.of(otherWishlist));
			given(wishlistAccommodationRepository.findById(wishlistAccommodationId))
				.willReturn(Optional.of(wishlistAccommodation));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));

			// When & Then
			assertThatThrownBy(() -> wishlistService.deleteWishlistAccommodation(
				otherWishlistId, wishlistAccommodationId, currentMemberId))
				.isInstanceOf(WishlistAccessDeniedException.class)
				.hasMessage("위시리스트에 대한 접근 권한이 없습니다.");

			verify(wishlistRepository).findById(otherWishlistId);
			verify(wishlistAccommodationRepository).findById(wishlistAccommodationId);
			verify(memberRepository).findById(currentMemberId);
			verify(wishlistAccommodationRepository, never()).delete(any());
		}

		@Test
		@DisplayName("다른 위시리스트에 속한 항목 삭제 시 예외 발생")
		void deleteWishlistAccommodation_WishlistAccommodationAccessDenied() {
			// Given
			Long wishlistId = 1L;
			Long otherWishlistAccommodationId = 20L; // 다른 위시리스트에 속한 항목
			Long currentMemberId = 1L;

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(wishlistAccommodationRepository.findById(otherWishlistAccommodationId))
				.willReturn(Optional.of(otherWishlistAccommodation));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));

			// When & Then
			assertThatThrownBy(() -> wishlistService.deleteWishlistAccommodation(
				wishlistId, otherWishlistAccommodationId, currentMemberId))
				.isInstanceOf(WishlistAccommodationAccessDeniedException.class)
				.hasMessage("위시리스트 항목에 대한 접근 권한이 없습니다.");

			verify(wishlistRepository).findById(wishlistId);
			verify(wishlistAccommodationRepository).findById(otherWishlistAccommodationId);
			verify(memberRepository).findById(currentMemberId);
			verify(wishlistAccommodationRepository, never()).delete(any());
		}

		@Test
		@DisplayName("여러 위시리스트 항목을 순차적으로 삭제할 수 있다")
		void deleteWishlistAccommodation_MultipleItemsSequentially() {
			// Given
			Long wishlistId = 1L;
			Long currentMemberId = 1L;
			Long[] wishlistAccommodationIds = {10L, 11L, 12L};

			for (int i = 0; i < wishlistAccommodationIds.length; i++) {
				Long wishlistAccommodationId = wishlistAccommodationIds[i];

				WishlistAccommodation accommodationToDelete = WishlistAccommodation.builder()
					.id(wishlistAccommodationId)
					.memo("삭제할 메모 " + (i + 1))
					.wishlist(wishlist)
					.accommodation(accommodation)
					.build();

				given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
				given(wishlistAccommodationRepository.findById(wishlistAccommodationId))
					.willReturn(Optional.of(accommodationToDelete));
				given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));

				// When
				assertThatCode(() -> wishlistService.deleteWishlistAccommodation(
					wishlistId, wishlistAccommodationId, currentMemberId))
					.doesNotThrowAnyException();

				// Then
				verify(wishlistAccommodationRepository).delete(accommodationToDelete);
			}

			verify(wishlistRepository, times(3)).findById(wishlistId);
			verify(wishlistAccommodationRepository).findById(10L);
			verify(wishlistAccommodationRepository).findById(11L);
			verify(wishlistAccommodationRepository).findById(12L);
			verify(memberRepository, times(3)).findById(currentMemberId);
			verify(wishlistAccommodationRepository, times(3)).delete(any(WishlistAccommodation.class));
		}

		@Test
		@DisplayName("위시리스트의 마지막 숙소를 삭제할 수 있다")
		void deleteWishlistAccommodation_LastItemInWishlist() {
			// Given
			Long wishlistId = 1L;
			Long lastWishlistAccommodationId = 10L;
			Long currentMemberId = 1L;

			// 마지막 숙소 항목
			WishlistAccommodation lastAccommodation = WishlistAccommodation.builder()
				.id(lastWishlistAccommodationId)
				.memo("마지막 남은 숙소")
				.wishlist(wishlist)
				.accommodation(accommodation)
				.build();

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(wishlistAccommodationRepository.findById(lastWishlistAccommodationId))
				.willReturn(Optional.of(lastAccommodation));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));

			// When
			assertThatCode(() -> wishlistService.deleteWishlistAccommodation(
				wishlistId, lastWishlistAccommodationId, currentMemberId))
				.doesNotThrowAnyException();

			// Then
			verify(wishlistRepository).findById(wishlistId);
			verify(wishlistAccommodationRepository).findById(lastWishlistAccommodationId);
			verify(memberRepository).findById(currentMemberId);
			verify(wishlistAccommodationRepository).delete(lastAccommodation);
		}

		@Test
		@DisplayName("같은 숙소 항목을 중복으로 삭제하려 시도할 수 없다")
		void deleteWishlistAccommodation_DuplicateDelete() {
			// Given
			Long wishlistId = 1L;
			Long wishlistAccommodationId = 10L;
			Long currentMemberId = 1L;

			// 첫 번째 삭제는 성공
			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(wishlistAccommodationRepository.findById(wishlistAccommodationId))
				.willReturn(Optional.of(wishlistAccommodation));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));

			// 첫 번째 삭제 수행
			wishlistService.deleteWishlistAccommodation(wishlistId, wishlistAccommodationId, currentMemberId);

			// 두 번째 삭제 시도 시 항목이 존재하지 않음
			given(wishlistAccommodationRepository.findById(wishlistAccommodationId))
				.willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> wishlistService.deleteWishlistAccommodation(
				wishlistId, wishlistAccommodationId, currentMemberId))
				.isInstanceOf(WishlistAccommodationNotFoundException.class)
				.hasMessage("존재하지 않는 위시리스트 항목입니다.");

			verify(wishlistRepository, times(2)).findById(wishlistId);
			verify(wishlistAccommodationRepository, times(2)).findById(wishlistAccommodationId);
			verify(memberRepository, times(1)).findById(currentMemberId); // 첫 번째만 성공
			verify(wishlistAccommodationRepository, times(1)).delete(wishlistAccommodation); // 한 번만 삭제
		}

		@Test
		@DisplayName("삭제할 때 올바른 항목이 삭제되는지 확인")
		void deleteWishlistAccommodation_CorrectItemDeleted() {
			// Given
			Long wishlistId = 1L;
			Long wishlistAccommodationId = 10L;
			Long currentMemberId = 1L;

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(wishlistAccommodationRepository.findById(wishlistAccommodationId))
				.willReturn(Optional.of(wishlistAccommodation));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));

			// When
			wishlistService.deleteWishlistAccommodation(wishlistId, wishlistAccommodationId, currentMemberId);

			// Then - ArgumentCaptor를 사용하여 삭제되는 객체 검증
			ArgumentCaptor<WishlistAccommodation> captor = ArgumentCaptor.forClass(WishlistAccommodation.class);
			verify(wishlistAccommodationRepository).delete(captor.capture());

			WishlistAccommodation deletedItem = captor.getValue();
			assertThat(deletedItem.getId()).isEqualTo(wishlistAccommodationId);
			assertThat(deletedItem.getWishlist()).isEqualTo(wishlist);
			assertThat(deletedItem.getAccommodation()).isEqualTo(accommodation);
			assertThat(deletedItem.getMemo()).isEqualTo("삭제할 메모");
		}

		@Test
		@DisplayName("동일한 숙소를 가진 다른 위시리스트 항목은 삭제되지 않는다")
		void deleteWishlistAccommodation_DoesNotAffectOtherWishlistItems() {
			// Given
			Long wishlistId = 1L;
			Long wishlistAccommodationId = 10L;
			Long currentMemberId = 1L;

			// 같은 숙소를 가진 다른 위시리스트 항목
			WishlistAccommodation sameAccommodationDifferentWishlist = WishlistAccommodation.builder()
				.id(30L)
				.memo("다른 위시리스트의 같은 숙소")
				.wishlist(otherWishlist)
				.accommodation(accommodation) // 같은 숙소
				.build();

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(wishlistAccommodationRepository.findById(wishlistAccommodationId))
				.willReturn(Optional.of(wishlistAccommodation));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));

			// When
			wishlistService.deleteWishlistAccommodation(wishlistId, wishlistAccommodationId, currentMemberId);

			// Then
			verify(wishlistAccommodationRepository).delete(wishlistAccommodation);
			// 다른 위시리스트의 같은 숙소는 삭제되지 않음을 확인
			verify(wishlistAccommodationRepository, never()).delete(sameAccommodationDifferentWishlist);
			verify(wishlistAccommodationRepository, never()).delete(otherWishlistAccommodation);
		}

		@Test
		@DisplayName("빈 위시리스트에서 항목 삭제 시도")
		void deleteWishlistAccommodation_EmptyWishlist() {
			// Given
			Long emptyWishlistId = 1L;
			Long nonExistentItemId = 999L;
			Long currentMemberId = 1L;

			Wishlist emptyWishlist = Wishlist.builder()
				.id(emptyWishlistId)
				.name("빈 위시리스트")
				.member(member)
				.build();

			given(wishlistRepository.findById(emptyWishlistId)).willReturn(Optional.of(emptyWishlist));
			given(wishlistAccommodationRepository.findById(nonExistentItemId))
				.willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> wishlistService.deleteWishlistAccommodation(
				emptyWishlistId, nonExistentItemId, currentMemberId))
				.isInstanceOf(WishlistAccommodationNotFoundException.class)
				.hasMessage("존재하지 않는 위시리스트 항목입니다.");

			verify(wishlistRepository).findById(emptyWishlistId);
			verify(wishlistAccommodationRepository).findById(nonExistentItemId);
			verify(memberRepository, never()).findById(any());
			verify(wishlistAccommodationRepository, never()).delete(any());
		}
	}

	@Nested
	@DisplayName("위시리스트 숙소 목록 조회 테스트")
	class FindWishlistAccommodationsTest {

		private WishlistAccommodation firstWishlistAccommodation;
		private WishlistAccommodation secondWishlistAccommodation;
		private WishlistAccommodation thirdWishlistAccommodation;
		private Accommodation firstAccommodation;
		private Accommodation secondAccommodation;
		private Accommodation thirdAccommodation;

		@BeforeEach
		void setUpAccommodations() {
			// 숙소 엔티티들
			firstAccommodation = Accommodation.builder()
				.id(100L)
				.name("신라호텔")
				.build();

			secondAccommodation = Accommodation.builder()
				.id(200L)
				.name("롯데호텔")
				.build();

			thirdAccommodation = Accommodation.builder()
				.id(300L)
				.name("게스트하우스")
				.build();

			// 위시리스트 항목들
			firstWishlistAccommodation = WishlistAccommodation.builder()
				.id(10L)
				.memo("신라호텔 메모")
				.wishlist(wishlist)
				.accommodation(firstAccommodation)
				.build();

			secondWishlistAccommodation = WishlistAccommodation.builder()
				.id(20L)
				.memo("롯데호텔 메모")
				.wishlist(wishlist)
				.accommodation(secondAccommodation)
				.build();

			thirdWishlistAccommodation = WishlistAccommodation.builder()
				.id(30L)
				.memo("게스트하우스 메모")
				.wishlist(wishlist)
				.accommodation(thirdAccommodation)
				.build();
		}

		@Test
		@DisplayName("정상적으로 위시리스트 숙소 목록을 조회한다")
		void findWishlistAccommodations_Success() {
			// Given
			Long wishlistId = 1L;
			Long currentMemberId = 1L;
			CursorRequest.CursorPageRequest request = CursorRequest.CursorPageRequest.builder()
				.size(20)
				.lastId(null)
				.lastCreatedAt(null)
				.build();

			List<WishlistAccommodation> wishlistAccommodations = List.of(
				firstWishlistAccommodation,
				secondWishlistAccommodation,
				thirdWishlistAccommodation
			);

			Slice<WishlistAccommodation> wishlistAccommodationSlice =
				new SliceImpl<>(wishlistAccommodations, PageRequest.of(0, 20), false);

			List<Long> wishlistAccommodationIds = List.of(10L, 20L, 30L);

			// Mock 설정
			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));
			given(wishlistAccommodationRepository.findByWishlistIdWithCursor(
				eq(wishlistId), eq(null), eq(null), any(PageRequest.class)))
				.willReturn(wishlistAccommodationSlice);

			// 이미지, 편의시설, 평점 Mock 설정
			given(wishlistAccommodationRepository.findAccommodationImagesByWishlistAccommodationIds(wishlistAccommodationIds))
				.willReturn(createMockImageProjections());
			given(wishlistAccommodationRepository.findAccommodationAmenitiesByWishlistAccommodationIds(wishlistAccommodationIds))
				.willReturn(createMockAmenityProjections());
			given(wishlistAccommodationRepository.findAccommodationRatingsByWishlistAccommodationIds(wishlistAccommodationIds))
				.willReturn(createMockRatingProjections());

			CursorResponse.PageInfo pageInfo = CursorResponse.PageInfo.builder()
				.hasNext(false)
				.nextCursor(null)
				.currentSize(3)
				.build();

			given(cursorPageInfoCreator.createPageInfo(eq(wishlistAccommodations), eq(false), any(), any()))
				.willReturn(pageInfo);

			// When
			WishlistResponse.WishlistAccommodationInfos response =
				wishlistService.findWishlistAccommodations(wishlistId, request, currentMemberId);

			// Then
			assertThat(response.wishlistAccommodations()).hasSize(3);
			assertThat(response.pageInfo().hasNext()).isFalse();
			assertThat(response.pageInfo().currentSize()).isEqualTo(3);

			// 첫 번째 항목 검증
			WishlistResponse.WishlistAccommodationInfo firstItem = response.wishlistAccommodations().get(0);
			assertThat(firstItem.id()).isEqualTo(10L);
			assertThat(firstItem.name()).isEqualTo("신라호텔 메모");
			assertThat(firstItem.accommodationInfo().accommodationId()).isEqualTo(100L);
			assertThat(firstItem.accommodationInfo().name()).isEqualTo("신라호텔");

			verify(wishlistRepository).findById(wishlistId);
			verify(memberRepository).findById(currentMemberId);
			verify(wishlistAccommodationRepository).findByWishlistIdWithCursor(
				eq(wishlistId), eq(null), eq(null), any(PageRequest.class));
			verify(wishlistAccommodationRepository).findAccommodationImagesByWishlistAccommodationIds(wishlistAccommodationIds);
			verify(wishlistAccommodationRepository).findAccommodationAmenitiesByWishlistAccommodationIds(wishlistAccommodationIds);
			verify(wishlistAccommodationRepository).findAccommodationRatingsByWishlistAccommodationIds(wishlistAccommodationIds);
		}

		@Test
		@DisplayName("빈 위시리스트의 숙소 목록을 조회한다")
		void findWishlistAccommodations_EmptyWishlist() {
			// Given
			Long emptyWishlistId = 1L;
			Long currentMemberId = 1L;
			CursorRequest.CursorPageRequest request = CursorRequest.CursorPageRequest.builder()
				.size(20)
				.lastId(null)
				.lastCreatedAt(null)
				.build();

			Slice<WishlistAccommodation> emptySlice =
				new SliceImpl<>(List.of(), PageRequest.of(0, 20), false);

			CursorResponse.PageInfo pageInfo = CursorResponse.PageInfo.builder()
				.hasNext(false)
				.nextCursor(null)
				.currentSize(0)
				.build();

			given(wishlistRepository.findById(emptyWishlistId)).willReturn(Optional.of(wishlist));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));
			given(wishlistAccommodationRepository.findByWishlistIdWithCursor(
				eq(emptyWishlistId), eq(null), eq(null), any(PageRequest.class)))
				.willReturn(emptySlice);
			given(cursorPageInfoCreator.createPageInfo(eq(List.of()), eq(false), any(), any()))
				.willReturn(pageInfo);

			// When
			WishlistResponse.WishlistAccommodationInfos response =
				wishlistService.findWishlistAccommodations(emptyWishlistId, request, currentMemberId);

			// Then
			assertThat(response.wishlistAccommodations()).isEmpty();
			assertThat(response.pageInfo().hasNext()).isFalse();
			assertThat(response.pageInfo().currentSize()).isZero();

			verify(wishlistRepository).findById(emptyWishlistId);
			verify(memberRepository).findById(currentMemberId);
			verify(wishlistAccommodationRepository).findByWishlistIdWithCursor(
				eq(emptyWishlistId), eq(null), eq(null), any(PageRequest.class));
			// 빈 리스트일 때는 이미지, 편의시설, 평점 조회하지 않음
			verify(wishlistAccommodationRepository, never()).findAccommodationImagesByWishlistAccommodationIds(any());
			verify(wishlistAccommodationRepository, never()).findAccommodationAmenitiesByWishlistAccommodationIds(any());
			verify(wishlistAccommodationRepository, never()).findAccommodationRatingsByWishlistAccommodationIds(any());
		}

		@Test
		@DisplayName("커서 기반 페이징으로 위시리스트 숙소 목록을 조회한다")
		void findWishlistAccommodations_WithCursor() {
			// Given
			Long wishlistId = 1L;
			Long currentMemberId = 1L;
			Long lastId = 20L;
			LocalDateTime lastCreatedAt = LocalDateTime.now().minusDays(1);

			CursorRequest.CursorPageRequest request = CursorRequest.CursorPageRequest.builder()
				.size(2)
				.lastId(lastId)
				.lastCreatedAt(lastCreatedAt)
				.build();

			List<WishlistAccommodation> pagedAccommodations = List.of(
				secondWishlistAccommodation,
				thirdWishlistAccommodation
			);

			Slice<WishlistAccommodation> wishlistAccommodationSlice =
				new SliceImpl<>(pagedAccommodations, PageRequest.of(0, 2), true);

			List<Long> wishlistAccommodationIds = List.of(20L, 30L);

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));
			given(wishlistAccommodationRepository.findByWishlistIdWithCursor(
				eq(wishlistId), eq(lastId), eq(lastCreatedAt), any(PageRequest.class)))
				.willReturn(wishlistAccommodationSlice);

			given(wishlistAccommodationRepository.findAccommodationImagesByWishlistAccommodationIds(wishlistAccommodationIds))
				.willReturn(createMockImageProjections());
			given(wishlistAccommodationRepository.findAccommodationAmenitiesByWishlistAccommodationIds(wishlistAccommodationIds))
				.willReturn(createMockAmenityProjections());
			given(wishlistAccommodationRepository.findAccommodationRatingsByWishlistAccommodationIds(wishlistAccommodationIds))
				.willReturn(createMockRatingProjections());

			CursorResponse.PageInfo pageInfo = CursorResponse.PageInfo.builder()
				.hasNext(true)
				.nextCursor("encoded_cursor")
				.currentSize(2)
				.build();

			given(cursorPageInfoCreator.createPageInfo(eq(pagedAccommodations), eq(true), any(), any()))
				.willReturn(pageInfo);

			// When
			WishlistResponse.WishlistAccommodationInfos response =
				wishlistService.findWishlistAccommodations(wishlistId, request, currentMemberId);

			// Then
			assertThat(response.wishlistAccommodations()).hasSize(2);
			assertThat(response.pageInfo().hasNext()).isTrue();
			assertThat(response.pageInfo().nextCursor()).isEqualTo("encoded_cursor");
			assertThat(response.pageInfo().currentSize()).isEqualTo(2);

			verify(wishlistRepository).findById(wishlistId);
			verify(memberRepository).findById(currentMemberId);
			verify(wishlistAccommodationRepository).findByWishlistIdWithCursor(
				eq(wishlistId), eq(lastId), eq(lastCreatedAt), any(PageRequest.class));
		}

		@Test
		@DisplayName("존재하지 않는 위시리스트 조회 시 예외 발생")
		void findWishlistAccommodations_WishlistNotFound() {
			// Given
			Long nonExistentWishlistId = 999L;
			Long currentMemberId = 1L;
			CursorRequest.CursorPageRequest request = CursorRequest.CursorPageRequest.builder()
				.size(20)
				.build();

			given(wishlistRepository.findById(nonExistentWishlistId)).willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> wishlistService.findWishlistAccommodations(
				nonExistentWishlistId, request, currentMemberId))
				.isInstanceOf(WishlistNotFoundException.class)
				.hasMessage("존재하지 않는 위시리스트입니다.");

			verify(wishlistRepository).findById(nonExistentWishlistId);
			verify(memberRepository, never()).findById(any());
			verify(wishlistAccommodationRepository, never()).findByWishlistIdWithCursor(any(), any(), any(), any());
		}

		@Test
		@DisplayName("존재하지 않는 사용자로 조회 시 예외 발생")
		void findWishlistAccommodations_MemberNotFound() {
			// Given
			Long wishlistId = 1L;
			Long nonExistentMemberId = 999L;
			CursorRequest.CursorPageRequest request = CursorRequest.CursorPageRequest.builder()
				.size(20)
				.build();

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(memberRepository.findById(nonExistentMemberId)).willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> wishlistService.findWishlistAccommodations(
				wishlistId, request, nonExistentMemberId))
				.isInstanceOf(MemberNotFoundException.class)
				.hasMessage("존재하지 않는 사용자입니다.");

			verify(wishlistRepository).findById(wishlistId);
			verify(memberRepository).findById(nonExistentMemberId);
			verify(wishlistAccommodationRepository, never()).findByWishlistIdWithCursor(any(), any(), any(), any());
		}

		@Test
		@DisplayName("다른 사용자의 위시리스트 조회 시 예외 발생")
		void findWishlistAccommodations_AccessDenied() {
			// Given
			Long otherWishlistId = 2L;
			Long currentMemberId = 1L;
			CursorRequest.CursorPageRequest request = CursorRequest.CursorPageRequest.builder()
				.size(20)
				.build();

			given(wishlistRepository.findById(otherWishlistId)).willReturn(Optional.of(otherWishlist));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));

			// When & Then
			assertThatThrownBy(() -> wishlistService.findWishlistAccommodations(
				otherWishlistId, request, currentMemberId))
				.isInstanceOf(WishlistAccessDeniedException.class)
				.hasMessage("위시리스트에 대한 접근 권한이 없습니다.");

			verify(wishlistRepository).findById(otherWishlistId);
			verify(memberRepository).findById(currentMemberId);
			verify(wishlistAccommodationRepository, never()).findByWishlistIdWithCursor(any(), any(), any(), any());
		}

		@Test
		@DisplayName("한 개의 숙소만 있는 위시리스트를 조회한다")
		void findWishlistAccommodations_SingleItem() {
			// Given
			Long wishlistId = 1L;
			Long currentMemberId = 1L;
			CursorRequest.CursorPageRequest request = CursorRequest.CursorPageRequest.builder()
				.size(20)
				.build();

			List<WishlistAccommodation> singleAccommodation = List.of(firstWishlistAccommodation);
			Slice<WishlistAccommodation> wishlistAccommodationSlice =
				new SliceImpl<>(singleAccommodation, PageRequest.of(0, 20), false);

			List<Long> wishlistAccommodationIds = List.of(10L);

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));
			given(wishlistAccommodationRepository.findByWishlistIdWithCursor(
				eq(wishlistId), eq(null), eq(null), any(PageRequest.class)))
				.willReturn(wishlistAccommodationSlice);

			given(wishlistAccommodationRepository.findAccommodationImagesByWishlistAccommodationIds(wishlistAccommodationIds))
				.willReturn(createMockImageProjections());
			given(wishlistAccommodationRepository.findAccommodationAmenitiesByWishlistAccommodationIds(wishlistAccommodationIds))
				.willReturn(createMockAmenityProjections());
			given(wishlistAccommodationRepository.findAccommodationRatingsByWishlistAccommodationIds(wishlistAccommodationIds))
				.willReturn(createMockRatingProjections());

			CursorResponse.PageInfo pageInfo = CursorResponse.PageInfo.builder()
				.hasNext(false)
				.nextCursor(null)
				.currentSize(1)
				.build();

			given(cursorPageInfoCreator.createPageInfo(eq(singleAccommodation), eq(false), any(), any()))
				.willReturn(pageInfo);

			// When
			WishlistResponse.WishlistAccommodationInfos response =
				wishlistService.findWishlistAccommodations(wishlistId, request, currentMemberId);

			// Then
			assertThat(response.wishlistAccommodations()).hasSize(1);
			assertThat(response.pageInfo().hasNext()).isFalse();
			assertThat(response.pageInfo().currentSize()).isEqualTo(1);

			WishlistResponse.WishlistAccommodationInfo item = response.wishlistAccommodations().get(0);
			assertThat(item.id()).isEqualTo(10L);
			assertThat(item.name()).isEqualTo("신라호텔 메모");
			assertThat(item.accommodationInfo().accommodationId()).isEqualTo(100L);
			assertThat(item.accommodationInfo().name()).isEqualTo("신라호텔");

			verify(wishlistRepository).findById(wishlistId);
			verify(memberRepository).findById(currentMemberId);
			verify(wishlistAccommodationRepository).findByWishlistIdWithCursor(
				eq(wishlistId), eq(null), eq(null), any(PageRequest.class));
		}

		@Test
		@DisplayName("사용자별로 위시리스트가 격리되어 조회된다")
		void findWishlistAccommodations_UserIsolation() {
			// Given
			Long wishlistId = 1L;
			Long currentMemberId = 1L;
			CursorRequest.CursorPageRequest request = CursorRequest.CursorPageRequest.builder()
				.size(20)
				.build();

			List<WishlistAccommodation> userAccommodations = List.of(firstWishlistAccommodation);
			Slice<WishlistAccommodation> wishlistAccommodationSlice =
				new SliceImpl<>(userAccommodations, PageRequest.of(0, 20), false);

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));
			given(wishlistAccommodationRepository.findByWishlistIdWithCursor(
				eq(wishlistId), eq(null), eq(null), any(PageRequest.class)))
				.willReturn(wishlistAccommodationSlice);

			given(wishlistAccommodationRepository.findAccommodationImagesByWishlistAccommodationIds(List.of(10L)))
				.willReturn(createMockImageProjections());
			given(wishlistAccommodationRepository.findAccommodationAmenitiesByWishlistAccommodationIds(List.of(10L)))
				.willReturn(createMockAmenityProjections());
			given(wishlistAccommodationRepository.findAccommodationRatingsByWishlistAccommodationIds(List.of(10L)))
				.willReturn(createMockRatingProjections());

			CursorResponse.PageInfo pageInfo = CursorResponse.PageInfo.builder()
				.hasNext(false)
				.nextCursor(null)
				.currentSize(1)
				.build();

			given(cursorPageInfoCreator.createPageInfo(eq(userAccommodations), eq(false), any(), any()))
				.willReturn(pageInfo);

			// When
			WishlistResponse.WishlistAccommodationInfos response =
				wishlistService.findWishlistAccommodations(wishlistId, request, currentMemberId);

			// Then
			assertThat(response.wishlistAccommodations()).hasSize(1);
			assertThat(response.wishlistAccommodations().get(0).id()).isEqualTo(10L);

			// 해당 위시리스트의 항목만 조회되는지 확인
			verify(wishlistAccommodationRepository).findByWishlistIdWithCursor(
				eq(wishlistId), eq(null), eq(null), any(PageRequest.class));
		}

		@Test
		@DisplayName("이미지, 편의시설, 평점이 없는 숙소도 조회된다")
		void findWishlistAccommodations_WithoutImageAmenityRating() {
			// Given
			Long wishlistId = 1L;
			Long currentMemberId = 1L;
			CursorRequest.CursorPageRequest request = CursorRequest.CursorPageRequest.builder()
				.size(20)
				.build();

			List<WishlistAccommodation> accommodations = List.of(firstWishlistAccommodation);
			Slice<WishlistAccommodation> wishlistAccommodationSlice =
				new SliceImpl<>(accommodations, PageRequest.of(0, 20), false);

			given(wishlistRepository.findById(wishlistId)).willReturn(Optional.of(wishlist));
			given(memberRepository.findById(currentMemberId)).willReturn(Optional.of(member));
			given(wishlistAccommodationRepository.findByWishlistIdWithCursor(
				eq(wishlistId), eq(null), eq(null), any(PageRequest.class)))
				.willReturn(wishlistAccommodationSlice);

			// 빈 리스트 반환 (이미지, 편의시설, 평점 없음)
			given(wishlistAccommodationRepository.findAccommodationImagesByWishlistAccommodationIds(List.of(10L)))
				.willReturn(List.of());
			given(wishlistAccommodationRepository.findAccommodationAmenitiesByWishlistAccommodationIds(List.of(10L)))
				.willReturn(List.of());
			given(wishlistAccommodationRepository.findAccommodationRatingsByWishlistAccommodationIds(List.of(10L)))
				.willReturn(List.of());

			CursorResponse.PageInfo pageInfo = CursorResponse.PageInfo.builder()
				.hasNext(false)
				.nextCursor(null)
				.currentSize(1)
				.build();

			given(cursorPageInfoCreator.createPageInfo(eq(accommodations), eq(false), any(), any()))
				.willReturn(pageInfo);

			// When
			WishlistResponse.WishlistAccommodationInfos response =
				wishlistService.findWishlistAccommodations(wishlistId, request, currentMemberId);

			// Then
			assertThat(response.wishlistAccommodations()).hasSize(1);

			WishlistResponse.WishlistAccommodationInfo item = response.wishlistAccommodations().get(0);
			assertThat(item.accommodationInfo().accommodationImageUrls()).isEmpty();
			assertThat(item.accommodationInfo().amenities()).isEmpty();
			assertThat(item.accommodationInfo().averageRating()).isNull();

			verify(wishlistRepository).findById(wishlistId);
			verify(memberRepository).findById(currentMemberId);
			verify(wishlistAccommodationRepository).findAccommodationImagesByWishlistAccommodationIds(List.of(10L));
			verify(wishlistAccommodationRepository).findAccommodationAmenitiesByWishlistAccommodationIds(List.of(10L));
			verify(wishlistAccommodationRepository).findAccommodationRatingsByWishlistAccommodationIds(List.of(10L));
		}

		// Helper methods for creating mock projections
		private List<WishlistImageProjection> createMockImageProjections() {
			return List.of(
				new WishlistImageProjection(10L, "hotel1_image1.jpg"),
				new WishlistImageProjection(10L, "hotel1_image2.jpg"),
				new WishlistImageProjection(20L, "hotel2_image1.jpg")
			);
		}

		private List<WishlistAmenityProjection> createMockAmenityProjections() {
			return List.of(
				new WishlistAmenityProjection(10L, AmenityType.WIFI, 1),
				new WishlistAmenityProjection(10L, AmenityType.TV, 1),
				new WishlistAmenityProjection(20L, AmenityType.PARKING, 1)
			);
		}

		private List<WishlistRatingProjection> createMockRatingProjections() {
			return List.of(
				new WishlistRatingProjection(10L, 4.5),
				new WishlistRatingProjection(20L, 4.3),
				new WishlistRatingProjection(30L, 4.0)
			);
		}
	}
}
