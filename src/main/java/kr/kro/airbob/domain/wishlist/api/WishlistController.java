package kr.kro.airbob.domain.wishlist.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.kro.airbob.cursor.annotation.CursorParam;
import kr.kro.airbob.cursor.dto.CursorRequest;
import kr.kro.airbob.domain.auth.AuthService;
import kr.kro.airbob.domain.auth.common.SessionUtil;
import kr.kro.airbob.domain.wishlist.WishlistService;
import kr.kro.airbob.domain.wishlist.dto.WishlistRequest;
import kr.kro.airbob.domain.wishlist.dto.WishlistResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/members/wishlists")
public class WishlistController {

	private final WishlistService wishlistService;

	@PostMapping
	public ResponseEntity<WishlistResponse.CreateResponse> createWishlist(
		@Valid @RequestBody WishlistRequest.createRequest requestDto,
		HttpServletRequest request) {

		Long memberId = (Long) request.getAttribute("memberId");

		log.info(requestDto.toString());
		WishlistResponse.CreateResponse response = wishlistService.createWishlist(requestDto, memberId);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{wishlistId}")
	public ResponseEntity<WishlistResponse.UpdateResponse> updateWishlist(
		@PathVariable Long wishlistId,
		@Valid @RequestBody WishlistRequest.updateRequest requestDto) {
		log.info(requestDto.toString());
		WishlistResponse.UpdateResponse response = wishlistService.updateWishlist(wishlistId, requestDto);
		log.info(response.toString());
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{wishlistId}")
	public ResponseEntity<Void> deleteWishlist(@PathVariable Long wishlistId) {
		log.info("{} 위시리스트 삭제 요청", wishlistId);
		wishlistService.deleteWishlist(wishlistId);
		log.info("{} 위시리스트 삭제 요청 완료", wishlistId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<WishlistResponse.WishlistInfos> findWishlists(
		@CursorParam CursorRequest.CursorPageRequest requestDto, HttpServletRequest request) {

		Long memberId = (Long) request.getAttribute("memberId");

		log.info(requestDto.toString());
		WishlistResponse.WishlistInfos response =
			wishlistService.findWishlists(requestDto, memberId);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{wishlistId}/accommodations")
	public ResponseEntity<WishlistResponse.CreateWishlistAccommodationResponse> createWishlistAccommodation(
		@PathVariable Long wishlistId,
		@Valid @RequestBody WishlistRequest.CreateWishlistAccommodationRequest requestDto) {
		log.info("{} 위시리스트에 {} 숙소 추가 요청", wishlistId, requestDto.accommodationId());
		WishlistResponse.CreateWishlistAccommodationResponse response =
			wishlistService.createWishlistAccommodation(wishlistId, requestDto);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{wishlistId}/accommodations/{wishlistAccommodationId}")
	public ResponseEntity<WishlistResponse.UpdateWishlistAccommodationResponse> updateWishlistAccommodation(
		@PathVariable Long wishlistId,
		@PathVariable Long wishlistAccommodationId,
		@Valid @RequestBody WishlistRequest.UpdateWishlistAccommodationRequest requestDto) {

		log.info("위시리스트 {} 안의 항목 {}의 메모 수정 요청 내용: {}"
			, wishlistId, wishlistAccommodationId, requestDto.toString());

		WishlistResponse.UpdateWishlistAccommodationResponse response =
			wishlistService.updateWishlistAccommodation(wishlistAccommodationId, requestDto);

		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{wishlistId}/accommodations/{wishlistAccommodationId}")
	public ResponseEntity<Void> deleteWishlistAccommodation(
		@PathVariable Long wishlistId,
		@PathVariable Long wishlistAccommodationId) {

		log.info("위시리스트 {} 안의 항목 {} 삭제 요청", wishlistId, wishlistAccommodationId);

		wishlistService.deleteWishlistAccommodation(wishlistAccommodationId);

		return ResponseEntity.noContent().build();
	}

	// todo: 추후 필터링 적용(날짜, 게스트 인원)
	@GetMapping("/{wishlistId}/accommodations")
	public ResponseEntity<WishlistResponse.WishlistAccommodationInfos> findWishlistAccommodations(
		@CursorParam CursorRequest.CursorPageRequest requestDto,
		@PathVariable Long wishlistId
	) {

		log.info("위시리스트: {} 조회 요청. cursor: {}", wishlistId, requestDto.toString());

		WishlistResponse.WishlistAccommodationInfos response
			= wishlistService.findWishlistAccommodations(wishlistId, requestDto);

		return ResponseEntity.ok(response);
	}
}
