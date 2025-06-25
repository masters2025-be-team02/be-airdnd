package kr.kro.airbob.geo;

import static kr.kro.airbob.geo.dto.GoogleGeocodeResponse.Geometry.*;

import org.springframework.stereotype.Component;

import kr.kro.airbob.geo.dto.Coordinate;

@Component
public class ViewportAdjuster {

	private static final double MIN_RADIUS_METERS = 3000; // 3km
	private static final double ONE_DEGREE = 111.0; // 위도 1도 ≈ 111km

	public Viewport adjustViewportIfSmall(Viewport originalViewport) {
		if (originalViewport == null) {
			return null;
		}

		double currentRadius = calculateViewportRadius(originalViewport);

		if (currentRadius < MIN_RADIUS_METERS) {
			return expandViewport(originalViewport);
		}

		return originalViewport;
	}

	public Viewport createViewportFromCenter(double latitude, double longitude) {
		double radiusKm = MIN_RADIUS_METERS / 1000.0;
		double latDelta = radiusKm / ONE_DEGREE;
		double lngDelta = radiusKm / (ONE_DEGREE * Math.cos(Math.toRadians(latitude)));

		Coordinate northeast = Coordinate.builder()
			.latitude(latitude + latDelta)
			.longitude(longitude + lngDelta)
			.build();

		Coordinate southwest = Coordinate.builder()
			.latitude(latitude - latDelta)
			.longitude(longitude - lngDelta)
			.build();

		return new Viewport(northeast, southwest);
	}

	private double calculateViewportRadius(Viewport viewport) {
		double distance = calculateDistance(viewport.northeast(), viewport.southwest());
		return distance / 2.0;
	}

	private double calculateDistance(Coordinate p1, Coordinate p2) {
		// m
		double latDiff = p1.latitude() - p2.latitude();
		double lngDiff = p1.longitude() - p2.longitude();

		// km
		double latDistance = Math.abs(latDiff) * ONE_DEGREE;
		double lngDistance = Math.abs(lngDiff) * ONE_DEGREE * Math.cos(Math.toRadians(p1.latitude()));

		return Math.sqrt(latDistance * latDistance + lngDistance * lngDistance) * 1000; // m
	}

	private Coordinate calculateCenter(Viewport viewport) {
		double centerLat = (viewport.northeast().latitude() + viewport.southwest().latitude()) / 2.0;
		double centerLng = (viewport.northeast().longitude() + viewport.southwest().longitude()) / 2.0;
		return Coordinate.builder()
			.latitude(centerLat)
			.longitude(centerLng)
			.build();
	}

	private Viewport expandViewport(Viewport originalViewport) {
		Coordinate center = calculateCenter(originalViewport);

		double radiusKm = MIN_RADIUS_METERS / 1000.0;
		double latDelta = radiusKm / ONE_DEGREE; //
		double lngDelta = radiusKm / (ONE_DEGREE * Math.cos(Math.toRadians(center.latitude())));

		Coordinate newNortheast = Coordinate.builder()
			.latitude(center.latitude() + latDelta)
			.longitude(center.longitude() + lngDelta)
			.build();

		Coordinate newSouthwest = Coordinate.builder()
			.latitude(center.latitude() - latDelta)
			.longitude(center.longitude() - lngDelta)
			.build();

		return new Viewport(newNortheast, newSouthwest);
	}
}


