package kr.kro.airbob.domain.accommodation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import kr.kro.airbob.domain.accommodation.common.AmenityType;
import kr.kro.airbob.domain.review.dto.ReviewResponse;
import kr.kro.airbob.geo.dto.Coordinate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class AccommodationResponse {

	private AccommodationResponse() {
	}

	public record WishlistAccommodationInfo(
		Long accommodationId,
		String name,
		List<String> accommodationImageUrls,
		List<AmenityInfoResponse> amenities,
		BigDecimal averageRating

	) {
	}

	public record AmenityInfoResponse(
		AmenityType type,
		Integer count
	) {
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class AccommodationSearchResponseDto {
		private String name;
		private String thumbnailUrl;
		private Integer pricePerNight;
		private Integer maxOccupancy;
		private List<AccommodationRequest.AmenityInfo> amenityInfos;
		private Double averageRating;
		private Integer reviewCount;
	}

	@Builder
	public record RecentlyViewedAccommodations(
		List<RecentlyViewedAccommodation> accommodations,
		int totalCount
	) {
	}

	@Builder
	public record RecentlyViewedAccommodation(
		LocalDateTime viewedAt,
		Long accommodationId,
		String accommodationName,
		String thumbnailUrl,
		List<AmenityInfoResponse> amenities,
		BigDecimal averageRating,
		Boolean isInWishlist
	) {
	}

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
