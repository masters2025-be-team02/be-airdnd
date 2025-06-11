package kr.kro.airbob.domain.wishlist;

import org.springframework.stereotype.Service;

import kr.kro.airbob.common.exception.MemberNotFoundException;
import kr.kro.airbob.domain.member.Member;
import kr.kro.airbob.domain.wishlist.dto.WishlistDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class WishlistService {

	private final WishlistRepository wishlistRepository;
	private final MemberRepository memberRepository;

	public WishlistDto.createResponse createWishlist(WishlistDto.createRequest request, Long currentMemberId) {

		Member member = memberRepository.findById(currentMemberId).orElseThrow(MemberNotFoundException::new);
		log.info(currentMemberId + " 사용자 조회 성공");

		Wishlist wishlist = Wishlist.builder()
			.name(request.name())
			.member(member)
			.build();

		Long id = wishlistRepository.save(wishlist);
		return new WishlistDto.createResponse(id);
	}
}
