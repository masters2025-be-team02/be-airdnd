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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import kr.kro.airbob.cursor.dto.CursorRequest;
import kr.kro.airbob.cursor.dto.CursorResponse;
import kr.kro.airbob.cursor.util.CursorPageInfoCreator;
import kr.kro.airbob.domain.member.Member;
import kr.kro.airbob.domain.member.MemberRepository;
import kr.kro.airbob.domain.member.common.MemberRole;
import kr.kro.airbob.domain.member.exception.MemberNotFoundException;
import kr.kro.airbob.domain.wishlist.dto.WishlistRequest;
import kr.kro.airbob.domain.wishlist.dto.WishlistResponse;
import kr.kro.airbob.domain.wishlist.exception.WishlistAccessDeniedException;
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
			WishlistResponse.createResponse response = wishlistService.createWishlist(request, currentMemberId);

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
			WishlistResponse.createResponse firstResponse = wishlistService.createWishlist(request, currentMemberId);
			WishlistResponse.createResponse secondResponse = wishlistService.createWishlist(request, currentMemberId);

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
			WishlistResponse.updateResponse response = wishlistService.updateWishlist(wishlistId, request, currentMemberId);

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
			WishlistResponse.updateResponse response = wishlistService.updateWishlist(wishlistId, request, currentMemberId);

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
}
