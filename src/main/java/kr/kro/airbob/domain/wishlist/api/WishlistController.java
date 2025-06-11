package kr.kro.airbob.domain.wishlist.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kr.kro.airbob.domain.wishlist.dto.WishlistDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class WishlistController {

	private final WishlistService wishlistService;

	@PostMapping("/members/wishlists")
	public ResponseEntity<WishlistDto.createResponse> createWishlist(
		@Valid @RequestBody WishlistDto.createRequest request) {
		log.info(request.toString());
		WishlistDto.createResponse response = wishlistService.createWishlist(request);
		log.info(response.toString());
		return ResponseEntity.ok(response);
	}
}
