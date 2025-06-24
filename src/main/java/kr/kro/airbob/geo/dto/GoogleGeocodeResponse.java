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
		Location location
	){
		public record  Location (
			Double lat,
			Double lng

		) {
		}
	}
}
