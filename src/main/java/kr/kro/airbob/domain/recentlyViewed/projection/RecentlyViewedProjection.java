package kr.kro.airbob.domain.recentlyViewed.projection;

public record RecentlyViewedProjection(
	Long id,
	String name,
	String thumbnailUrl,
	Double averageRating
) {
}
