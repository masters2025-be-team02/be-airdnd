package kr.kro.airbob.domain.wishlist.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kr.kro.airbob.domain.wishlist.dto.WishlistRequest;
import kr.kro.airbob.domain.wishlist.dto.WishlistResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/members/recentlyViewed")
public class RecentlyViewedController {

	private static final Long TEMP_LOGGED_IN_MEMBER_ID = 1L;

	private final RecentlyViewedService recentlyViewedService;
	@PostMapping
	public ResponseEntity<WishlistResponse.CreateRecentlyViewedResponse> createRecentlyViewed(
		@Valid @RequestBody WishlistRequest.CreateRecentlyViewedRequest request) {

		 log.info("{} 사용자가 {} 숙소 조회", TEMP_LOGGED_IN_MEMBER_ID, request.recentlyViewedId());

		WishlistResponse.CreateRecentlyViewedResponse response
			= recentlyViewedService.createRecentlyViewed(request, TEMP_LOGGED_IN_MEMBER_ID);

		return ResponseEntity.ok(response);
	}
}
