package kr.kro.airbob.domain.wishlist;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.kro.airbob.domain.accommodation.entity.Accommodation;
import kr.kro.airbob.domain.accommodation.exception.AccommodationNotFoundException;
import kr.kro.airbob.domain.accommodation.repository.AccommodationRepository;
import kr.kro.airbob.domain.member.exception.MemberNotFoundException;
import kr.kro.airbob.cursor.dto.CursorRequest;
import kr.kro.airbob.cursor.dto.CursorResponse;
import kr.kro.airbob.cursor.util.CursorPageInfoCreator;
import kr.kro.airbob.domain.member.Member;
import kr.kro.airbob.domain.member.MemberRepository;
import kr.kro.airbob.domain.member.common.MemberRole;
import kr.kro.airbob.domain.wishlist.dto.WishlistRequest;
import kr.kro.airbob.domain.wishlist.dto.WishlistResponse;
import kr.kro.airbob.domain.wishlist.exception.WishlistAccessDeniedException;
import kr.kro.airbob.domain.wishlist.exception.WishlistAccommodationAccessDeniedException;
import kr.kro.airbob.domain.wishlist.exception.WishlistAccommodationNotFoundException;
import kr.kro.airbob.domain.wishlist.exception.WishlistNotFoundException;
import kr.kro.airbob.domain.wishlist.repository.WishlistAccommodationRepository;
import kr.kro.airbob.domain.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class WishlistService {

	private final MemberRepository memberRepository;
	private final WishlistRepository wishlistRepository;
	private final AccommodationRepository accommodationRepository;
	private final WishlistAccommodationRepository wishlistAccommodationRepository;

	private final CursorPageInfoCreator cursorPageInfoCreator;

	@Transactional
	public WishlistResponse.CreateResponse createWishlist(WishlistRequest.createRequest request, Long currentMemberId) {

		Member member = findMemberById(currentMemberId);
		log.info("{} 사용자 조회 성공", member.getId());

		Wishlist wishlist = Wishlist.builder()
			.name(request.name())
			.member(member)
			.build();

		Wishlist savedWishlist = wishlistRepository.save(wishlist);
		return new WishlistResponse.CreateResponse(savedWishlist.getId());
	}

	@Transactional
	public WishlistResponse.UpdateResponse updateWishlist(Long wishlistId, WishlistRequest.updateRequest request, Long currentMemberId) {

		Wishlist foundWishlist = findWishlistById(wishlistId);
		log.info("{} 위시리스트 조회 성공", foundWishlist.getId());

		validateWishlistOwnership(foundWishlist, currentMemberId);

		log.info("위시리스트 이름 {} -> {} 변경", foundWishlist.getName(), request.name());
		foundWishlist.updateName(request.name());

		return new WishlistResponse.UpdateResponse(foundWishlist.getId());
	}

	@Transactional
	public void deleteWishlist(Long wishlistId, Long currentMemberId) {
		// 위시리스트 존재, 작성자 id 검증을 위한 조회
		Wishlist foundWishlist = findWishlistById(wishlistId);
		log.info("{} 위시리스트 조회 성공", foundWishlist.getId());

		Member currentMember = findMemberById(currentMemberId);

		validateWishlistOwnershipOrAdmin(foundWishlist, currentMember);

		// 위시리스트에 속한 숙소 삭제
		wishlistAccommodationRepository.deleteAllByWishlistId(foundWishlist.getId());
		wishlistRepository.delete(foundWishlist);
	}

	@Transactional(readOnly = true)
	public WishlistResponse.WishlistInfos findWishlists(CursorRequest.CursorPageRequest request, Long currentMemberId) {

		Member member = findMemberById(currentMemberId); // 사용자 존재 여부를 위해 넣었는데, 필요한지 의문. member는 사용하지 않음

		Long lastId = request.lastId();
		LocalDateTime lastCreatedAt = request.lastCreatedAt();

		Slice<Wishlist> wishlistSlice = wishlistRepository.findByMemberIdWithCursor(
			currentMemberId,
			lastId,
			lastCreatedAt,
			PageRequest.of(0, request.size())
		);
		log.info("위시리스크 목록 조회: {} 개, 다음 페이지 여부: {}", wishlistSlice.getContent().size(), wishlistSlice.hasNext());

		List<Long> wishlistIds = wishlistSlice.getContent().stream()
			.map(Wishlist::getId)
			.toList();

		// 위시리스트별 숙소 개수 조회
		Map<Long, Long> wishlistItemCounts = wishlistAccommodationRepository.countByWishlistIds(wishlistIds);

		// 위시리스트별 가장 최근에 추가된 숙소 썸네일 Url 조회
		Map<Long, String> thumbnailUrls = wishlistAccommodationRepository.findLatestThumbnailUrlsByWishlistIds(wishlistIds);

		List<WishlistResponse.WishlistInfo> wishlistInfos = wishlistSlice.getContent().stream()
			.map(wishlist ->
				new WishlistResponse.WishlistInfo(
					wishlist.getId(),
					wishlist.getName(),
					wishlist.getCreatedAt(),
					wishlistItemCounts.getOrDefault(wishlist.getId(), 0L),
					thumbnailUrls.get(wishlist.getId()) // nullable
				)).toList();

		CursorResponse.PageInfo pageInfo = cursorPageInfoCreator.createPageInfo(
			wishlistSlice.getContent(),
			wishlistSlice.hasNext(),
			Wishlist::getId,
			Wishlist::getCreatedAt
		);

		return new WishlistResponse.WishlistInfos(wishlistInfos, pageInfo);
	}

	@Transactional(readOnly = true)
	public WishlistResponse.CreateWishlistAccommodationResponse createWishlistAccommodation(Long wishlistId,
		WishlistRequest.CreateWishlistAccommodationRequest request, Long currentMemberId) {
		Wishlist wishlist = findWishlistById(wishlistId);
		Accommodation accommodation = findAccommodationById(request.accommodationId());
		Member member = findMemberById(currentMemberId);

		WishlistAccommodation wishlistAccommodation = WishlistAccommodation.builder()
			.wishlist(wishlist)
			.accommodation(accommodation)
			.build();

		WishlistAccommodation savedWishlistAccommodation
			= wishlistAccommodationRepository.save(wishlistAccommodation);

		return new WishlistResponse.CreateWishlistAccommodationResponse(savedWishlistAccommodation.getId());
	}

	@Transactional
	public WishlistResponse.UpdateWishlistAccommodationResponse updateWishlistAccommodation(Long wishlistId,
		Long wishlistAccommodationId, WishlistRequest.UpdateWishlistAccommodationRequest request,
		Long currentMemberId) {

		Wishlist wishlist = findWishlistById(wishlistId);
		WishlistAccommodation wishlistAccommodation = findWishlistAccommodation(wishlistAccommodationId);

		Member member = findMemberById(currentMemberId);

		validateWishlistOwnership(wishlist, member.getId());
		validateWishlistAccommodationOwnership(wishlistAccommodation, wishlistId);

		wishlistAccommodation.updateMemo(request.memo());

		return new WishlistResponse.UpdateWishlistAccommodationResponse(wishlistAccommodation.getId());
	}

	@Transactional
	public void deleteWishlistAccommodation(Long wishlistId, Long wishlistAccommodationId, Long currentMemberId) {

		Wishlist wishlist = findWishlistById(wishlistId);
		WishlistAccommodation wishlistAccommodation = findWishlistAccommodation(wishlistAccommodationId);

		Member member = findMemberById(currentMemberId);

		validateWishlistOwnership(wishlist, member.getId());
		validateWishlistAccommodationOwnership(wishlistAccommodation, wishlistId);

		wishlistAccommodationRepository.delete(wishlistAccommodation);
	}

	private Wishlist findWishlistById(Long wishlistId) {
		return wishlistRepository.findById(wishlistId).orElseThrow(WishlistNotFoundException::new);
	}

	private Member findMemberById(Long currentMemberId) {
		return memberRepository.findById(currentMemberId).orElseThrow(MemberNotFoundException::new);
	}

	private Accommodation findAccommodationById(Long accommodationId) {
		return accommodationRepository.findById(accommodationId).orElseThrow(AccommodationNotFoundException::new);
	}

	private WishlistAccommodation findWishlistAccommodation(Long wishlistAccommodationId){
		return wishlistAccommodationRepository.findById(wishlistAccommodationId)
			.orElseThrow(WishlistAccommodationNotFoundException::new);
	}

	private void validateWishlistOwnership(Wishlist wishlist, Long memberId) {
		if (!wishlist.isOwnedBy(memberId)) {
			log.error("사용자 아이디: {}, 위시리스트 작성자 아이디: {}", memberId, wishlist.getMember().getId());
			throw new WishlistAccessDeniedException();
		}
	}

	private void validateWishlistOwnershipOrAdmin(Wishlist wishlist, Member member) {
		if (!wishlist.isOwnedBy(member.getId()) && member.getRole() != MemberRole.ADMIN) {
			log.error("사용자 아이디: {}, 위시리스트 작성자 아이디: {}", member.getId(), wishlist.getMember().getId());
			throw new WishlistAccessDeniedException();
		}
	}

	private void validateWishlistAccommodationOwnership(WishlistAccommodation wishlistAccommodation, Long wishlistId) {
		if (!wishlistAccommodation.isOwnedBy(wishlistId)) {
			log.error("위시리스트: {}, 항목이 속한 위시리스트: {}", wishlistId, wishlistAccommodation.getAccommodation().getId());
			throw new WishlistAccommodationAccessDeniedException();
		}
	}
}
