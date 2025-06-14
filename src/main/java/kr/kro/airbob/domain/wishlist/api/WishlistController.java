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

import jakarta.validation.Valid;
import kr.kro.airbob.cursor.annotation.CursorParam;
import kr.kro.airbob.cursor.dto.CursorRequest;
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
	private static final Long TEMP_LOGGED_IN_MEMBER_ID = 1L;

	@PostMapping
	public ResponseEntity<WishlistResponse.CreateResponse> createWishlist(
		@Valid @RequestBody WishlistRequest.createRequest request) {
		log.info(request.toString());
		WishlistResponse.CreateResponse response = wishlistService.createWishlist(request, TEMP_LOGGED_IN_MEMBER_ID);
		log.info(response.toString());
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{wishlistId}")
	public ResponseEntity<WishlistResponse.UpdateResponse> updateWishlist(
		@PathVariable Long wishlistId,
		@Valid @RequestBody WishlistRequest.updateRequest request) {
		log.info(request.toString());
		WishlistResponse.UpdateResponse response = wishlistService.updateWishlist(wishlistId, request,
			TEMP_LOGGED_IN_MEMBER_ID);
		log.info(response.toString());
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{wishlistId}")
	public ResponseEntity<Void> deleteWishlist(@PathVariable Long wishlistId) {
		log.info("{} 위시리스트 삭제 요청", wishlistId);
		wishlistService.deleteWishlist(wishlistId, TEMP_LOGGED_IN_MEMBER_ID);
		log.info("{} 위시리스트 삭제 요청 완료", wishlistId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<WishlistResponse.WishlistInfos> findWishlists(
		@CursorParam CursorRequest.CursorPageRequest request) {
		log.info(request.toString());
		WishlistResponse.WishlistInfos response =
			wishlistService.findWishlists(request, TEMP_LOGGED_IN_MEMBER_ID);
		log.info(response.toString());
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{wishlistId}/accommodations")
	public ResponseEntity<WishlistResponse.AddAccommodationResponse> addAccommodationToWishlist(
		@PathVariable Long wishlistId,
		@Valid @RequestBody WishlistRequest.AddAccommodationResponse request) {
		log.info("{} 위시리스트에 {} 숙소 추가 요청", wishlistId, request.accommodationId());
		WishlistResponse.AddAccommodationResponse response =
			wishlistService.addAccommodationToWishlist(wishlistId, request, TEMP_LOGGED_IN_MEMBER_ID);
		return ResponseEntity.ok(response);
	}
}
