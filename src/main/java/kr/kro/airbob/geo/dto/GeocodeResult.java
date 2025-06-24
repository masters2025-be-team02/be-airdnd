package kr.kro.airbob.geo.dto;

import lombok.Builder;

@Builder
public record GeocodeResult(
	Double latitude,
	Double longitude,
	String formattedAddress,
	boolean success
) {
	public static GeocodeResult fail() {
		return GeocodeResult.builder()
			.success(false)
			.build();
	}
}
