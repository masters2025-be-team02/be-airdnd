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
import kr.kro.airbob.common.annotation.CursorParam;
import kr.kro.airbob.common.dto.CursorRequest;
import kr.kro.airbob.domain.wishlist.WishlistService;
import kr.kro.airbob.domain.wishlist.dto.WishlistRequest;
import kr.kro.airbob.domain.wishlist.dto.WishlistResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class WishlistController {

	private final WishlistService wishlistService;
	private static final Long TEMP_LOGGED_IN_MEMBER_ID = 1L;

	@PostMapping("/members/wishlists")
	public ResponseEntity<WishlistResponse.createResponse> createWishlist(
		@Valid @RequestBody WishlistRequest.createRequest request) {
		log.info(request.toString());
		WishlistResponse.createResponse response = wishlistService.createWishlist(request, TEMP_LOGGED_IN_MEMBER_ID);
		log.info(response.toString());
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/members/wishlists/{wishlistId}")
	public ResponseEntity<WishlistResponse.updateResponse> updateWishlist(
		@PathVariable Long wishlistId,
		@Valid @RequestBody WishlistRequest.updateRequest request) {
		log.info(request.toString());
		WishlistResponse.updateResponse response = wishlistService.updateWishlist(wishlistId, request,
			TEMP_LOGGED_IN_MEMBER_ID);
		log.info(response.toString());
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/members/wishlists/{wishlistId}")
	public ResponseEntity<Void> deleteWishlist(@PathVariable Long wishlistId) {
		log.info("{} 위시리스트 삭제 요청", wishlistId);
		wishlistService.deleteWishlist(wishlistId, TEMP_LOGGED_IN_MEMBER_ID);
		log.info("{} 위시리스트 삭제 요청 완료", wishlistId);
		return ResponseEntity.noContent().build();
	}
	
}
