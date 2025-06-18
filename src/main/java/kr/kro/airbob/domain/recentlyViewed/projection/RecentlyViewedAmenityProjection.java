package kr.kro.airbob.domain.recentlyViewed.projection;

import kr.kro.airbob.domain.accommodation.common.AmenityType;

public record RecentlyViewedAmenityProjection(
	Long accommodationId,
	AmenityType type,
	Integer count
) {
}
