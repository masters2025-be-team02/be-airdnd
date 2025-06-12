package kr.kro.airbob.domain.wishlist;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.kro.airbob.common.exception.MemberNotFoundException;
import kr.kro.airbob.domain.member.Member;
import kr.kro.airbob.domain.member.MemberRepository;
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

		Member member = memberRepository.findById(currentMemberId).orElseThrow(MemberNotFoundException::new);
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

		Wishlist foundWishlist = wishlistRepository.findById(wishlistId).orElseThrow(WishlistNotFoundException::new);
		log.info("{} 위시리스트 조회 성공", foundWishlist.getId());

		if (foundWishlist.getMember().getId() != currentMemberId) { // validator 클래스 고려
			log.error("사용자 아이디: {}, 위시리스트 작성자 아이디: {}", currentMemberId, foundWishlist.getMember().getId());
			throw new WishlistAccessDeniedException();
		}

		log.info("위시리스트 이름 {} -> {} 변경", foundWishlist.getName(), request.name());
		foundWishlist.updateName(request.name());

		return new WishlistResponse.updateResponse(foundWishlist.getId());
	}
}
