package kr.kro.airbob.domain.wishlist;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import kr.kro.airbob.config.QueryDslConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import kr.kro.airbob.domain.member.Member;
import kr.kro.airbob.domain.member.common.MemberRole;
import kr.kro.airbob.domain.wishlist.Wishlist;
import kr.kro.airbob.domain.wishlist.repository.WishlistRepository;

@DataJpaTest
@Testcontainers
@Import(QueryDslConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("위시리스트 레포지토리 테스트")
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
	private TestEntityManager entityManager;

	@Autowired
	private WishlistRepository wishlistRepository;

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

		entityManager.persistAndFlush(member1);
		entityManager.persistAndFlush(member2);

		// 위시리스트 데이터 생성 (생성 시간을 다르게 설정)
		LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);

		// Member1의 위시리스트들 (최신순으로 정렬됨)
		wishlist1 = createAndPersistWishlist("서울 여행", member1, baseTime.plusDays(4)); // 가장 최신
		wishlist2 = createAndPersistWishlist("부산 여행", member1, baseTime.plusDays(3));
		wishlist3 = createAndPersistWishlist("제주 여행", member1, baseTime.plusDays(2));
		wishlist4 = createAndPersistWishlist("대구 여행", member1, baseTime.plusDays(1));

		// Member2의 위시리스트
		wishlist5 = createAndPersistWishlist("광주 여행", member2, baseTime.plusDays(5));

		entityManager.flush();
		entityManager.clear();
	}

	@Test
	@DisplayName("회원별 위시리스트를 생성시간 내림차순으로 조회한다")
	void findByMemberIdWithCursor_OrderByCreatedAtDesc() {
		// Given
		Long memberId = member1.getId();
		PageRequest pageRequest = PageRequest.of(0, 10);

		// When
		Slice<Wishlist> result = wishlistRepository.findByMemberIdWithCursor(
			memberId, null, null, pageRequest);

		// Then
		assertThat(result.getContent()).hasSize(4);
		assertThat(result.hasNext()).isFalse();

		// 생성 시간 내림차순 확인
		List<Wishlist> wishlists = result.getContent();
		assertThat(wishlists.get(0).getName()).isEqualTo("서울 여행"); // 가장 최신
		assertThat(wishlists.get(1).getName()).isEqualTo("부산 여행");
		assertThat(wishlists.get(2).getName()).isEqualTo("제주 여행");
		assertThat(wishlists.get(3).getName()).isEqualTo("대구 여행"); // 가장 오래된
	}

	@Test
	@DisplayName("회원별로 위시리스트가 분리되어 조회된다")
	void findByMemberIdWithCursor_FilterByMember() {
		// Given
		Long member1Id = member1.getId();
		Long member2Id = member2.getId();
		PageRequest pageRequest = PageRequest.of(0, 10);

		// When
		Slice<Wishlist> member1Result = wishlistRepository.findByMemberIdWithCursor(
			member1Id, null, null, pageRequest);
		Slice<Wishlist> member2Result = wishlistRepository.findByMemberIdWithCursor(
			member2Id, null, null, pageRequest);

		// Then
		assertThat(member1Result.getContent()).hasSize(4);
		assertThat(member2Result.getContent()).hasSize(1);

		// Member1의 위시리스트만 조회되는지 확인
		member1Result.getContent().forEach(wishlist ->
			assertThat(wishlist.getMember().getId()).isEqualTo(member1Id));

		// Member2의 위시리스트만 조회되는지 확인
		member2Result.getContent().forEach(wishlist ->
			assertThat(wishlist.getMember().getId()).isEqualTo(member2Id));
	}

	@Test
	@DisplayName("커서 기반 페이징이 정상적으로 동작한다 - lastCreatedAt만 사용")
	void findByMemberIdWithCursor_WithLastCreatedAt() {
		// Given
		Long memberId = member1.getId();
		PageRequest pageRequest = PageRequest.of(0, 2);

		// 첫 번째 페이지 조회
		Slice<Wishlist> firstPage = wishlistRepository.findByMemberIdWithCursor(
			memberId, null, null, pageRequest);

		// When - 두 번째 페이지 조회 (lastCreatedAt만 사용)
		Wishlist lastWishlistOfFirstPage = firstPage.getContent().getLast();
		Slice<Wishlist> secondPage = wishlistRepository.findByMemberIdWithCursor(
			memberId, null, lastWishlistOfFirstPage.getCreatedAt(), pageRequest);

		// Then
		assertThat(firstPage.getContent()).hasSize(2);
		assertThat(firstPage.hasNext()).isTrue();
		assertThat(firstPage.getContent().get(0).getName()).isEqualTo("서울 여행");
		assertThat(firstPage.getContent().get(1).getName()).isEqualTo("부산 여행");

		assertThat(secondPage.getContent()).hasSize(2);
		assertThat(secondPage.hasNext()).isFalse();
		assertThat(secondPage.getContent().get(0).getName()).isEqualTo("제주 여행");
		assertThat(secondPage.getContent().get(1).getName()).isEqualTo("대구 여행");
	}

	@Test
	@DisplayName("커서 기반 페이징이 정상적으로 동작한다 - lastId와 lastCreatedAt 모두 사용")
	void findByMemberIdWithCursor_WithLastIdAndCreatedAt() {
		// Given
		Long memberId = member1.getId();
		PageRequest pageRequest = PageRequest.of(0, 2);

		// 첫 번째 페이지 조회
		Slice<Wishlist> firstPage = wishlistRepository.findByMemberIdWithCursor(
			memberId, null, null, pageRequest);

		// When - 두 번째 페이지 조회 (lastId와 lastCreatedAt 모두 사용)
		Wishlist lastWishlistOfFirstPage = firstPage.getContent().get(firstPage.getContent().size() - 1);
		Slice<Wishlist> secondPage = wishlistRepository.findByMemberIdWithCursor(
			memberId,
			lastWishlistOfFirstPage.getId(),
			lastWishlistOfFirstPage.getCreatedAt(),
			pageRequest);

		// Then
		assertThat(firstPage.getContent()).hasSize(2);
		assertThat(firstPage.hasNext()).isTrue();

		assertThat(secondPage.getContent()).hasSize(2);
		assertThat(secondPage.hasNext()).isFalse();

		// 중복 조회 방지 확인 (lastId 이후의 데이터만 조회)
		assertThat(secondPage.getContent())
			.noneMatch(wishlist -> wishlist.getId().equals(lastWishlistOfFirstPage.getId()));
	}

	@Test
	@DisplayName("동일한 생성시간을 가진 위시리스트도 정확히 페이징된다")
	void findByMemberIdWithCursor_WithSameCreatedAt() {
		// Given - 동일한 생성시간을 가진 위시리스트 추가 생성
		LocalDateTime sameTime = LocalDateTime.of(2024, 6, 1, 12, 0, 0);

		Wishlist sameTime1 = createAndPersistWishlist("동시간1", member1, sameTime);
		Wishlist sameTime2 = createAndPersistWishlist("동시간2", member1, sameTime);
		Wishlist sameTime3 = createAndPersistWishlist("동시간3", member1, sameTime);

		entityManager.flush();
		entityManager.clear();

		// When
		Long memberId = member1.getId();
		PageRequest pageRequest = PageRequest.of(0, 2);

		// 첫 번째 페이지 조회 (동일한 시간의 위시리스트들이 포함됨)
		Slice<Wishlist> firstPage = wishlistRepository.findByMemberIdWithCursor(
			memberId, null, null, pageRequest);

		// Then
		assertThat(firstPage.getContent()).hasSize(2);
		assertThat(firstPage.hasNext()).isTrue();

		// 동일한 시간의 위시리스트들이 ID 순서로 정렬되는지 확인
		List<Wishlist> sameTimeWishlists = firstPage.getContent().stream()
			.filter(w -> w.getCreatedAt().equals(sameTime))
			.toList();

		if (sameTimeWishlists.size() > 1) {
			for (int i = 0; i < sameTimeWishlists.size() - 1; i++) {
				assertThat(sameTimeWishlists.get(i).getId())
					.isGreaterThan(sameTimeWishlists.get(i + 1).getId());
			}
		}
	}

	@Test
	@DisplayName("존재하지 않는 회원 ID로 조회 시 빈 결과를 반환한다")
	void findByMemberIdWithCursor_NonExistentMember() {
		// Given
		Long nonExistentMemberId = 999L;
		PageRequest pageRequest = PageRequest.of(0, 10);

		// When
		Slice<Wishlist> result = wishlistRepository.findByMemberIdWithCursor(
			nonExistentMemberId, null, null, pageRequest);

		// Then
		assertThat(result.getContent()).isEmpty();
		assertThat(result.hasNext()).isFalse();
	}

	@Test
	@DisplayName("페이지 크기가 1일 때 정상 동작한다")
	void findByMemberIdWithCursor_SmallPageSize() {
		// Given
		Long memberId = member1.getId();
		PageRequest pageRequest = PageRequest.of(0, 1);

		// When
		Slice<Wishlist> result = wishlistRepository.findByMemberIdWithCursor(
			memberId, null, null, pageRequest);

		// Then
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.hasNext()).isTrue(); // 데이터가 더 있으므로 다음 페이지 존재
		assertThat(result.getContent().get(0).getName()).isEqualTo("서울 여행"); // 가장 최신
	}

	@Test
	@DisplayName("매우 큰 페이지 크기로 조회해도 정상 동작한다")
	void findByMemberIdWithCursor_LargePageSize() {
		// Given
		Long memberId = member1.getId();
		PageRequest pageRequest = PageRequest.of(0, 1000);

		// When
		Slice<Wishlist> result = wishlistRepository.findByMemberIdWithCursor(
			memberId, null, null, pageRequest);

		// Then
		assertThat(result.getContent()).hasSize(4); // member1의 위시리스트 4개
		assertThat(result.hasNext()).isFalse();
	}

	@Test
	@DisplayName("위시리스트가 없는 회원의 경우 빈 결과를 반환한다")
	void findByMemberIdWithCursor_MemberWithNoWishlists() {
		// Given - 위시리스트가 없는 새 회원 생성
		Member newMember = Member.builder()
			.email("newmember@example.com")
			.password("password123")
			.nickname("신규회원")
			.role(MemberRole.MEMBER)
			.build();

		entityManager.persistAndFlush(newMember);

		PageRequest pageRequest = PageRequest.of(0, 10);

		// When
		Slice<Wishlist> result = wishlistRepository.findByMemberIdWithCursor(
			newMember.getId(), null, null, pageRequest);

		// Then
		assertThat(result.getContent()).isEmpty();
		assertThat(result.hasNext()).isFalse();
	}

	@Test
	@DisplayName("잘못된 커서 정보로 조회 시에도 안전하게 처리된다")
	void findByMemberIdWithCursor_InvalidCursor() {
		// Given
		Long memberId = member1.getId();
		Long invalidLastId = 999999L; // 존재하지 않는 ID
		LocalDateTime futureTime = LocalDateTime.of(2030, 1, 1, 0, 0, 0); // 미래 시간
		PageRequest pageRequest = PageRequest.of(0, 10);

		// When
		Slice<Wishlist> result = wishlistRepository.findByMemberIdWithCursor(
			memberId, invalidLastId, futureTime, pageRequest);

		// Then
		assertThat(result.getContent()).hasSize(4); // 모든 위시리스트가 조회됨
		assertThat(result.hasNext()).isFalse();
	}

	// 헬퍼 메서드
	private Wishlist createAndPersistWishlist(String name, Member member, LocalDateTime createdAt) {
		Wishlist wishlist = Wishlist.builder()
			.name(name)
			.member(member)
			.build();

		entityManager.persist(wishlist);

		// createdAt을 수동으로 설정하기 위해 직접 SQL 실행
		entityManager.flush();
		entityManager.getEntityManager()
			.createNativeQuery("UPDATE wishlist SET created_at = ? WHERE id = ?")
			.setParameter(1, createdAt)
			.setParameter(2, wishlist.getId())
			.executeUpdate();

		entityManager.refresh(wishlist);
		return wishlist;
	}
}
