package kr.kro.airbob.search.api;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import kr.kro.airbob.geo.ClientIpExtractor;
import kr.kro.airbob.search.dto.AccommodationSearchRequest;
import kr.kro.airbob.search.dto.AccommodationSearchResponse;
import kr.kro.airbob.search.service.AccommodationSearchService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class AccommodationSearchController {

	private final AccommodationSearchService accommodationSearchService;
	private final ClientIpExtractor clientIpExtractor;

	private static final int DEFAULT_PAGE_SIZE = 18;
	private static final int MAX_PAGE_NUMBER = 14;

	@GetMapping("/accommodations")
	public ResponseEntity<AccommodationSearchResponse.AccommodationSearchInfos> searchAccommodations(
		@ModelAttribute AccommodationSearchRequest.MapBoundsDto mapBounds,
		@ModelAttribute AccommodationSearchRequest.AccommodationSearchRequestDto searchRequest,
		@PageableDefault(size = DEFAULT_PAGE_SIZE, page = 0) Pageable pageable,
		HttpServletRequest request) {

		if (pageable.getPageNumber() > MAX_PAGE_NUMBER) {
			pageable = PageRequest.of(MAX_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
		}

		if (pageable.getPageSize() != DEFAULT_PAGE_SIZE) {
			pageable = PageRequest.of(pageable.getPageNumber(), DEFAULT_PAGE_SIZE);
		}

		Long memberId = (Long)request.getAttribute("memberId");
		String clientIp = clientIpExtractor.extractClientIp(request);

		AccommodationSearchResponse.AccommodationSearchInfos infos =
			accommodationSearchService.searchAccommodations(searchRequest, memberId, clientIp, mapBounds, pageable);

		return ResponseEntity.ok(infos);
	}
}
