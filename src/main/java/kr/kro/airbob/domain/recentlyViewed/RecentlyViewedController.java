package kr.kro.airbob.domain.recentlyViewed;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/members/recentlyViewed")
public class RecentlyViewedController {

	private final RecentlyViewedService recentlyViewedService;

	@PostMapping("/{accommodationId}")
	public ResponseEntity<Void> addRecentlyViewed(@PathVariable Long accommodationId, HttpServletRequest request) {

		Long memberId = (Long) request.getAttribute("memberId");

		recentlyViewedService.addRecentlyViewed(memberId, accommodationId);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{accommodationId}")
	public ResponseEntity<Void> removeRecentlyViewed(
		@PathVariable Long accommodationId,
		HttpServletRequest request) {
		Long memberId = (Long) request.getAttribute("memberId");
		recentlyViewedService.removeRecentlyViewed(memberId, accommodationId);
		return ResponseEntity.ok().build();
	}


}
