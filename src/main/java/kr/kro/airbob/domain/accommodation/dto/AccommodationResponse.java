package kr.kro.airbob.domain.accommodation.dto;

import java.time.LocalDateTime;
import java.util.List;

import kr.kro.airbob.domain.accommodation.common.AmenityType;
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
		Double averageRating

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

	public record RecentlyViewedAccommodations(
		List<RecentlyViewedAccommodation> accommodations,
		int totalCount
	) {

	}

	public record RecentlyViewedAccommodation(
		LocalDateTime viewedAt,
		Long accommodationId,
		String accommodationName,
		String thumbnailUrl,
		List<AmenityInfoResponse> amenities,
		Double reviewRating,
		Boolean isInWishlist
	) {
	}
}
