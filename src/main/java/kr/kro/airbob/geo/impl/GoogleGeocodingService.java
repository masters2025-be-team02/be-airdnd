package kr.kro.airbob.geo.impl;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest;
import kr.kro.airbob.geo.GeocodingService;
import kr.kro.airbob.geo.dto.GeocodeResult;
import kr.kro.airbob.geo.dto.GoogleGeocodeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleGeocodingService implements GeocodingService {

	private final RestTemplate restTemplate;

	// todo: 배포 후엔 api ip 제한 걸기
	@Value("${google.api.key}")
	private String googleApiKey;

	private static final String GEOCODING_API_URL = "https://maps.googleapis.com/maps/api/geocode/json";
	@Override
	public GeocodeResult getCoordinates(String address) {
		try {

			URI baseUri = URI.create(GEOCODING_API_URL);

			String url = UriComponentsBuilder.fromUri(baseUri)
				.queryParam("address", address)
				.queryParam("key", googleApiKey)
				.queryParam("language", "ko")
				.build()
				.toUriString();

			GoogleGeocodeResponse response = restTemplate.getForObject(url, GoogleGeocodeResponse.class);

			if (response != null && "OK".equals(response.getStatus()) && !response.getResults().isEmpty()) {
				GoogleGeocodeResponse.Result result = response.getResults().get(0);
				GoogleGeocodeResponse.Geometry.Location location = result.geometry().location();

				return GeocodeResult.builder()
					.latitude(location.lat())
					.longitude(location.lng())
					.formattedAddress(result.formattedAddress())
					.success(true)
					.build();
			} else {
				log.warn("Geocoding 실패: {}, status: {}", address, response != null ? response.getStatus() : "null");
				return GeocodeResult.fail();
			}
		} catch (Exception e) {
			log.error("Geocoding API 호출 중 오류 발생: {}", address, e);
			return GeocodeResult.fail();
		}
	}

	@Override
	public String buildAddressString(AccommodationRequest.AddressInfo addressInfo) {
		return String.format("%s %s %s %s %s",
				addressInfo.getCountry(),
				addressInfo.getCity(),
				addressInfo.getDistrict(),
				addressInfo.getStreet(),
				addressInfo.getDetail())
			.replaceAll("\\s", " ")
			.trim();
	}
}
