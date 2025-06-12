package kr.kro.airbob.domain.wishlist;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.kro.airbob.common.exception.MemberNotFoundException;
import kr.kro.airbob.domain.member.Member;
import kr.kro.airbob.domain.member.MemberRepository;
import kr.kro.airbob.domain.wishlist.dto.WishlistRequest;
import kr.kro.airbob.domain.wishlist.dto.WishlistResponse;
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

		Member member = memberRepository.findById(currentMemberId).orElseThrow(MemberNotFoundException::new);
		log.info("{} 사용자 조회 성공", member.getId());

		Wishlist wishlist = Wishlist.builder()
			.name(request.name())
			.member(member)
			.build();

		Wishlist savedWishlist = wishlistRepository.save(wishlist);
		return new WishlistResponse.createResponse(savedWishlist.getId());
	}
}
