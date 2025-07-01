package kr.kro.airbob.search.service;

import org.springframework.stereotype.Service;

import kr.kro.airbob.search.dto.AccommodationSearchRequest;
import kr.kro.airbob.search.dto.AccommodationSearchResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccommodationSearchService {
	public AccommodationSearchResponse.AccommodationSearchInfos searchAccommodations(
		AccommodationSearchRequest.AccommodationSearchRequestDto searchRequest, Long memberId, String clientIp,
		AccommodationSearchRequest.MapBoundsDto mapBounds) {
		return null;
	}
}
