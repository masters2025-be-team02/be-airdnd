package kr.kro.airbob.domain.wishlist;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import kr.kro.airbob.cursor.dto.CursorRequest;
import kr.kro.airbob.domain.accommodation.entity.Accommodation;
import kr.kro.airbob.domain.accommodation.entity.Address;
import kr.kro.airbob.domain.accommodation.exception.AccommodationNotFoundException;
import kr.kro.airbob.domain.accommodation.repository.AccommodationRepository;
import kr.kro.airbob.domain.accommodation.repository.AddressRepository;
import kr.kro.airbob.domain.member.Member;
import kr.kro.airbob.domain.member.MemberRepository;
import kr.kro.airbob.domain.member.common.MemberRole;
import kr.kro.airbob.domain.member.exception.MemberNotFoundException;
import kr.kro.airbob.domain.wishlist.dto.WishlistRequest;
import kr.kro.airbob.domain.wishlist.dto.WishlistResponse;
import kr.kro.airbob.domain.wishlist.exception.WishlistAccessDeniedException;
import kr.kro.airbob.domain.wishlist.exception.WishlistAccommodationDuplicateException;
import kr.kro.airbob.domain.wishlist.exception.WishlistNotFoundException;
import kr.kro.airbob.domain.wishlist.repository.WishlistAccommodationRepository;
import kr.kro.airbob.domain.wishlist.repository.WishlistRepository;

@SpringBootTest
@Transactional
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("위시리스트 Service-Repository 통합 테스트")
class WishlistIntegrationTest {

	@Container
	static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
		.withDatabaseName("testdb")
		.withUsername("test")
		.withPassword("test");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysql::getJdbcUrl);
		registry.add("spring.datasource.username", mysql::getUsername);
		registry.add("spring.datasource.password", mysql::getPassword);
		registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);

		registry.add("spring.flyway.url", mysql::getJdbcUrl);
		registry.add("spring.flyway.user", mysql::getUsername);
		registry.add("spring.flyway.password", mysql::getPassword);
	}

	@Autowired
	private WishlistService wishlistService;

	@Autowired
	private WishlistRepository wishlistRepository;

	@Autowired
	private WishlistAccommodationRepository wishlistAccommodationRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private AccommodationRepository accommodationRepository;

	@Autowired
	private AddressRepository addressRepository;

	private Member member1;
	private Member member2;
	private Wishlist wishlist1;
	private Wishlist wishlist2;
	private Wishlist wishlist3;
	private Wishlist wishlist4;
	private Wishlist wishlist5;

	@BeforeEach
	void setUp() {
		// 회원 데이터 생성
		member1 = Member.builder()
			.email("member1@example.com")
			.password("password123")
			.nickname("회원1")
			.role(MemberRole.MEMBER)
			.build();

		member2 = Member.builder()
			.email("member2@example.com")
			.password("password123")
			.nickname("회원2")
			.role(MemberRole.MEMBER)
			.build();

		member1 = memberRepository.save(member1);
		member2 = memberRepository.save(member2);

		// 위시리스트 데이터 생성
		wishlist1 = createAndSaveWishlist("서울 여행", member1);
		wishlist2 = createAndSaveWishlist("부산 여행", member1);
		wishlist3 = createAndSaveWishlist("제주 여행", member1);
		wishlist4 = createAndSaveWishlist("대구 여행", member1);

		// Member2의 위시리스트
		wishlist5 = createAndSaveWishlist("광주 여행", member2);
	}

	private Wishlist createAndSaveWishlist(String name, Member member) {
		Wishlist wishlist = Wishlist.builder()
			.name(name)
			.member(member)
			.build();
		return wishlistRepository.save(wishlist);
	}

	private Address createAndSaveAddress(String city) {
		Address address = Address.builder()
			.postalCode(12345)
			.city(city)
			.country("KR")
			.district("District")
			.street("Street")
			.detail("Detail")
			.build();
		return addressRepository.save(address);
	}

	private Accommodation createAndSaveAccommodation(String name, Address address) {
		Accommodation accommodation = Accommodation.builder()
			.name(name)
			.address(address)
			.thumbnailUrl("https://example.com/test.jpg")
			.build();
		return accommodationRepository.save(accommodation);
	}

	@Nested
	@DisplayName("위시리스트 생성 테스트")
	class CreateWishlistTest {

		@Test
		@DisplayName("정상적으로 위시리스트를 생성한다")
		void createWishlist_Success() {
			// Given
			WishlistRequest.createRequest request = new WishlistRequest.createRequest("새로운 여행 계획");
			Long memberId = member1.getId();

			// When
			WishlistResponse.CreateResponse response = wishlistService.createWishlist(request, memberId);

			// Then
			assertThat(response).isNotNull();
			assertThat(response.id()).isNotNull();

			// 데이터베이스에서 실제로 저장되었는지 확인
			Wishlist savedWishlist = wishlistRepository.findById(response.id()).orElse(null);
			assertThat(savedWishlist).isNotNull();
			assertThat(savedWishlist.getName()).isEqualTo("새로운 여행 계획");
			assertThat(savedWishlist.getMember().getId()).isEqualTo(memberId);
		}

		@Test
		@DisplayName("존재하지 않는 회원으로 위시리스트 생성 시 예외 발생")
		void createWishlist_MemberNotFound() {
			// Given
			WishlistRequest.createRequest request = new WishlistRequest.createRequest("테스트 위시리스트");
			Long nonExistentMemberId = 999L;

			// When & Then
			assertThatThrownBy(() -> wishlistService.createWishlist(request, nonExistentMemberId))
				.isInstanceOf(MemberNotFoundException.class)
				.hasMessage("존재하지 않는 사용자입니다.");
		}

		@Test
		@DisplayName("같은 이름의 위시리스트를 여러 개 생성할 수 있다")
		void createWishlist_DuplicateNameAllowed() {
			// Given
			WishlistRequest.createRequest request = new WishlistRequest.createRequest("중복 이름");
			Long memberId = member1.getId();

			// When
			WishlistResponse.CreateResponse firstResponse = wishlistService.createWishlist(request, memberId);
			WishlistResponse.CreateResponse secondResponse = wishlistService.createWishlist(request, memberId);

			// Then
			assertThat(firstResponse.id()).isNotEqualTo(secondResponse.id());

			Wishlist firstWishlist = wishlistRepository.findById(firstResponse.id()).orElse(null);
			Wishlist secondWishlist = wishlistRepository.findById(secondResponse.id()).orElse(null);

			assertThat(firstWishlist).isNotNull();
			assertThat(secondWishlist).isNotNull();
			assertThat(firstWishlist.getName()).isEqualTo("중복 이름");
			assertThat(secondWishlist.getName()).isEqualTo("중복 이름");
		}
	}

	@Nested
	@DisplayName("위시리스트 수정 테스트")
	class UpdateWishlistTest {

		@Test
		@DisplayName("정상적으로 위시리스트를 수정한다")
		void updateWishlist_Success() {
			// Given
			WishlistRequest.updateRequest request = new WishlistRequest.updateRequest("수정된 서울 여행");
			Long wishlistId = wishlist1.getId();
			Long memberId = member1.getId();

			// When
			WishlistResponse.UpdateResponse response = wishlistService.updateWishlist(wishlistId, request, memberId);

			// Then
			assertThat(response.id()).isEqualTo(wishlistId);

			Wishlist updatedWishlist = wishlistRepository.findById(wishlistId).orElse(null);
			assertThat(updatedWishlist).isNotNull();
			assertThat(updatedWishlist.getName()).isEqualTo("수정된 서울 여행");
		}

		@Test
		@DisplayName("존재하지 않는 위시리스트 수정 시 예외 발생")
		void updateWishlist_WishlistNotFound() {
			// Given
			WishlistRequest.updateRequest request = new WishlistRequest.updateRequest("수정된 이름");
			Long nonExistentWishlistId = 999L;
			Long memberId = member1.getId();

			// When & Then
			assertThatThrownBy(() -> wishlistService.updateWishlist(nonExistentWishlistId, request, memberId))
				.isInstanceOf(WishlistNotFoundException.class)
				.hasMessage("존재하지 않는 위시리스트입니다.");
		}

		@Test
		@DisplayName("다른 사용자의 위시리스트 수정 시 예외 발생")
		void updateWishlist_AccessDenied() {
			// Given
			WishlistRequest.updateRequest request = new WishlistRequest.updateRequest("수정된 이름");
			Long wishlistId = wishlist1.getId(); // member1의 위시리스트
			Long otherMemberId = member2.getId(); // 다른 회원

			// When & Then
			assertThatThrownBy(() -> wishlistService.updateWishlist(wishlistId, request, otherMemberId))
				.isInstanceOf(WishlistAccessDeniedException.class)
				.hasMessage("위시리스트에 대한 접근 권한이 없습니다.");
		}
	}

	@Nested
	@DisplayName("위시리스트 삭제 테스트")
	class DeleteWishlistTest {

		@Test
		@DisplayName("정상적으로 위시리스트를 삭제한다")
		void deleteWishlist_Success() {
			// Given
			Long wishlistId = wishlist1.getId();
			Long memberId = member1.getId();

			// When
			assertThatCode(() -> wishlistService.deleteWishlist(wishlistId, memberId))
				.doesNotThrowAnyException();

			// Then
			assertThat(wishlistRepository.findById(wishlistId)).isEmpty();
		}

		@Test
		@DisplayName("존재하지 않는 위시리스트 삭제 시 예외 발생")
		void deleteWishlist_WishlistNotFound() {
			// Given
			Long nonExistentWishlistId = 999L;
			Long memberId = member1.getId();

			// When & Then
			assertThatThrownBy(() -> wishlistService.deleteWishlist(nonExistentWishlistId, memberId))
				.isInstanceOf(WishlistNotFoundException.class)
				.hasMessage("존재하지 않는 위시리스트입니다.");
		}

		@Test
		@DisplayName("다른 사용자의 위시리스트 삭제 시 예외 발생")
		void deleteWishlist_AccessDenied() {
			// Given
			Long wishlistId = wishlist1.getId(); // member1의 위시리스트
			Long otherMemberId = member2.getId(); // 다른 회원

			// When & Then
			assertThatThrownBy(() -> wishlistService.deleteWishlist(wishlistId, otherMemberId))
				.isInstanceOf(WishlistAccessDeniedException.class)
				.hasMessage("위시리스트에 대한 접근 권한이 없습니다.");
		}
	}

	@Nested
	@DisplayName("위시리스트 목록 조회 테스트")
	class FindWishlistsTest {

		@Test
		@DisplayName("회원별 위시리스트를 조회한다")
		void findWishlists_Success() {
			// Given
			Long memberId = member1.getId();
			CursorRequest.CursorPageRequest request = CursorRequest.CursorPageRequest.builder()
				.size(10)
				.lastId(null)
				.lastCreatedAt(null)
				.build();

			// When
			WishlistResponse.WishlistInfos response = wishlistService.findWishlists(request, memberId);

			// Then
			assertThat(response.wishlists()).hasSize(4);

			List<WishlistResponse.WishlistInfo> wishlists = response.wishlists();
			List<String> wishlistNames = wishlists.stream()
				.map(WishlistResponse.WishlistInfo::name)
				.toList();

			assertThat(wishlistNames).containsExactlyInAnyOrder("서울 여행", "부산 여행", "제주 여행", "대구 여행");
		}

		@Test
		@DisplayName("회원별로 위시리스트가 분리되어 조회된다")
		void findWishlists_FilterByMember() {
			// Given
			Long member1Id = member1.getId();
			Long member2Id = member2.getId();
			CursorRequest.CursorPageRequest request = CursorRequest.CursorPageRequest.builder()
				.size(10)
				.lastId(null)
				.lastCreatedAt(null)
				.build();

			// When
			WishlistResponse.WishlistInfos member1Result = wishlistService.findWishlists(request, member1Id);
			WishlistResponse.WishlistInfos member2Result = wishlistService.findWishlists(request, member2Id);

			// Then
			assertThat(member1Result.wishlists()).hasSize(4);
			assertThat(member2Result.wishlists()).hasSize(1);
			assertThat(member2Result.wishlists().get(0).name()).isEqualTo("광주 여행");
		}

		@Test
		@DisplayName("존재하지 않는 회원으로 위시리스트 조회 시 예외 발생")
		void findWishlists_MemberNotFound() {
			// Given
			Long nonExistentMemberId = 999L;
			CursorRequest.CursorPageRequest request = CursorRequest.CursorPageRequest.builder()
				.size(10)
				.lastId(null)
				.lastCreatedAt(null)
				.build();

			// When & Then
			assertThatThrownBy(() -> wishlistService.findWishlists(request, nonExistentMemberId))
				.isInstanceOf(MemberNotFoundException.class)
				.hasMessage("존재하지 않는 사용자입니다.");
		}
	}

	@Nested
	@DisplayName("위시리스트 숙소 추가 테스트")
	class CreateWishlistAccommodationTest {

		@Test
		@DisplayName("위시리스트에 숙소를 성공적으로 추가한다")
		void createWishlistAccommodation_Success() {
			// Given
			Address address = createAndSaveAddress("청주");
			Accommodation accommodation = createAndSaveAccommodation("제주도 해변 리조트", address);

			WishlistRequest.CreateWishlistAccommodationRequest request =
				new WishlistRequest.CreateWishlistAccommodationRequest(accommodation.getId());

			// When
			WishlistResponse.CreateWishlistAccommodationResponse response =
				wishlistService.createWishlistAccommodation(wishlist1.getId(), request, member1.getId());

			// Then
			assertThat(response).isNotNull();
			assertThat(response.id()).isNotNull();

			// 데이터베이스에서 실제로 저장되었는지 확인
			WishlistAccommodation savedWishlistAccommodation =
				wishlistAccommodationRepository.findById(response.id()).orElse(null);

			assertThat(savedWishlistAccommodation).isNotNull();
			assertThat(savedWishlistAccommodation.getWishlist().getId()).isEqualTo(wishlist1.getId());
			assertThat(savedWishlistAccommodation.getAccommodation().getId()).isEqualTo(accommodation.getId());
		}

		@Test
		@DisplayName("존재하지 않는 위시리스트에 숙소 추가 시 예외가 발생한다")
		void createWishlistAccommodation_WishlistNotFound() {
			// Given
			Long nonExistentWishlistId = 999L;
			Address address = createAndSaveAddress("청주");
			Accommodation accommodation = createAndSaveAccommodation("테스트 숙소", address);

			WishlistRequest.CreateWishlistAccommodationRequest request =
				new WishlistRequest.CreateWishlistAccommodationRequest(accommodation.getId());

			// When & Then
			assertThatThrownBy(() ->
				wishlistService.createWishlistAccommodation(nonExistentWishlistId, request, member1.getId()))
				.isInstanceOf(WishlistNotFoundException.class)
				.hasMessage("존재하지 않는 위시리스트입니다.");
		}

		@Test
		@DisplayName("다른 사용자의 위시리스트에 숙소 추가 시 예외가 발생한다")
		void createWishlistAccommodation_AccessDenied() {
			// Given
			Address address = createAndSaveAddress("청주");
			Accommodation accommodation = createAndSaveAccommodation("테스트 숙소", address);

			WishlistRequest.CreateWishlistAccommodationRequest request =
				new WishlistRequest.CreateWishlistAccommodationRequest(accommodation.getId());

			// When & Then - member2가 member1의 위시리스트에 접근 시도
			assertThatThrownBy(() ->
				wishlistService.createWishlistAccommodation(wishlist1.getId(), request, member2.getId()))
				.isInstanceOf(WishlistAccessDeniedException.class)
				.hasMessage("위시리스트에 대한 접근 권한이 없습니다.");
		}

		@Test
		@DisplayName("존재하지 않는 숙소를 위시리스트에 추가 시 예외가 발생한다")
		void createWishlistAccommodation_AccommodationNotFound() {
			// Given
			Long nonExistentAccommodationId = 999L;

			WishlistRequest.CreateWishlistAccommodationRequest request =
				new WishlistRequest.CreateWishlistAccommodationRequest(nonExistentAccommodationId);

			// When & Then
			assertThatThrownBy(() ->
				wishlistService.createWishlistAccommodation(wishlist1.getId(), request, member1.getId()))
				.isInstanceOf(AccommodationNotFoundException.class)
				.hasMessage("존재하지 않는 숙소입니다.");
		}

		@Test
		@DisplayName("같은 숙소를 같은 위시리스트에 중복 추가 시 예외가 발생한다")
		void createWishlistAccommodation_DuplicateAccommodation() {
			// Given
			Address address = createAndSaveAddress("청주");
			Accommodation accommodation = createAndSaveAccommodation("중복 테스트 숙소", address);

			WishlistRequest.CreateWishlistAccommodationRequest request =
				new WishlistRequest.CreateWishlistAccommodationRequest(accommodation.getId());

			// When - 첫 번째 추가는 성공
			WishlistResponse.CreateWishlistAccommodationResponse firstResponse =
				wishlistService.createWishlistAccommodation(wishlist1.getId(), request, member1.getId());

			assertThat(firstResponse).isNotNull();

			// Then - 두 번째 추가는 실패
			assertThatThrownBy(() ->
				wishlistService.createWishlistAccommodation(wishlist1.getId(), request, member1.getId()))
				.isInstanceOf(WishlistAccommodationDuplicateException.class)
				.hasMessage("이미 위시리스트에 추가된 숙소입니다.");
		}
	}
}
