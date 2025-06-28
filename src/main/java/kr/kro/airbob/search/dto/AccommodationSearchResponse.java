package kr.kro.airbob.search.dto;

import java.util.List;

import kr.kro.airbob.domain.review.dto.ReviewResponse;
import kr.kro.airbob.geo.dto.Coordinate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccommodationSearchResponse {

	@Builder
	public record AccommodationSearchInfo(
		long id,
		String name,
		String locationSummary, // ex) 동작구 사당동
		List<String> accommodationImageUrls,
		Coordinate coordinate,
		PriceResponse pricePerNight,
		ReviewResponse.ReviewSummary review,
		String hostName,
		Boolean isInWishlist
	){
	}

	@Builder
	public record AccommodationSearchInfos(
		List<AccommodationSearchInfo> StaySearchResultListing,
		int totalCount
	){
	}


	@Builder
	public record  PriceResponse(
		String currencyCode,
		String displayPrice,
		int price
	){
	}
}
