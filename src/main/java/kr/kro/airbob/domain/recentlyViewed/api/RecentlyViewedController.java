package kr.kro.airbob.domain.recentlyViewed.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.kro.airbob.domain.recentlyViewed.RecentlyViewedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/members/recentlyViewed")
public class RecentlyViewedController {

	private static final Long TEMP_LOGGED_IN_MEMBER_ID = 1L;

	private final RecentlyViewedService recentlyViewedService;


}
