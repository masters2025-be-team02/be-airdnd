package kr.kro.airbob.geo.dto;

import java.util.List;

import lombok.Getter;

@Getter
public class GoogleGeocodeResponse {

	private String status;
	private List<Result> results;

	public record Result(
		String formattedAddress,
		Geometry geometry
		) {
	}

	public record Geometry(
		Coordinate location,
		Viewport viewport
	){
		public record Viewport(
			Coordinate northeast,
			Coordinate southwest
		) {
		}
	}
}
