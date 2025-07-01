package kr.kro.airbob.search.api;

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

	@GetMapping("/accommodations")
	public ResponseEntity<AccommodationSearchResponse.AccommodationSearchInfos> searchAccommodations(
		@ModelAttribute AccommodationSearchRequest.MapBoundsDto mapBounds,
		@ModelAttribute AccommodationSearchRequest.AccommodationSearchRequestDto searchRequest,
		HttpServletRequest request) {

		Long memberId = (Long)request.getAttribute("memberId");
		String clientIp = clientIpExtractor.extractClientIp(request);

		AccommodationSearchResponse.AccommodationSearchInfos infos =
			accommodationSearchService.searchAccommodations(searchRequest, memberId, clientIp, mapBounds);

		return ResponseEntity.ok(infos);
	}
}
