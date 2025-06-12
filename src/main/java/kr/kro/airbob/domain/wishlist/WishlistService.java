package kr.kro.airbob.domain.wishlist;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.kro.airbob.common.exception.MemberNotFoundException;
import kr.kro.airbob.domain.member.Member;
import kr.kro.airbob.domain.member.MemberRepository;
import kr.kro.airbob.domain.member.common.MemberRole;
import kr.kro.airbob.domain.wishlist.dto.WishlistRequest;
import kr.kro.airbob.domain.wishlist.dto.WishlistResponse;
import kr.kro.airbob.domain.wishlist.exception.WishlistAccessDeniedException;
import kr.kro.airbob.domain.wishlist.exception.WishlistNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class WishlistService {

	private final WishlistRepository wishlistRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public WishlistResponse.createResponse createWishlist(WishlistRequest.createRequest request, Long currentMemberId) {

		Member member = findMemberById(currentMemberId);
		log.info("{} 사용자 조회 성공", member.getId());

		Wishlist wishlist = Wishlist.builder()
			.name(request.name())
			.member(member)
			.build();

		Wishlist savedWishlist = wishlistRepository.save(wishlist);
		return new WishlistResponse.createResponse(savedWishlist.getId());
	}

	@Transactional
	public WishlistResponse.updateResponse updateWishlist(Long wishlistId, WishlistRequest.updateRequest request, Long currentMemberId) {

		Wishlist foundWishlist = findWishlistById(wishlistId);
		log.info("{} 위시리스트 조회 성공", foundWishlist.getId());

		validateWishlistOwnership(foundWishlist, currentMemberId);

		log.info("위시리스트 이름 {} -> {} 변경", foundWishlist.getName(), request.name());
		foundWishlist.updateName(request.name());

		return new WishlistResponse.updateResponse(foundWishlist.getId());
	}

	@Transactional
	public void deleteWishlist(Long wishlistId, Long currentMemberId) {
		// 위시리스트 존재, 작성자 id 검증을 위한 조회
		Wishlist foundWishlist = findWishlistById(wishlistId);
		log.info("{} 위시리스트 조회 성공", foundWishlist.getId());

		Member currentMember = findMemberById(currentMemberId);

		validateWishlistOwnershipOrAdmin(foundWishlist, currentMember);

		wishlistRepository.delete(foundWishlist);
	}

	private Wishlist findWishlistById(Long wishlistId) {
		return wishlistRepository.findById(wishlistId).orElseThrow(WishlistNotFoundException::new);
	}

	private Member findMemberById(Long currentMemberId) {
		return memberRepository.findById(currentMemberId).orElseThrow(MemberNotFoundException::new);
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
}
