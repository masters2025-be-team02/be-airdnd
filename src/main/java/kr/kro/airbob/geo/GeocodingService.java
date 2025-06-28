package kr.kro.airbob.geo;

import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest;
import kr.kro.airbob.geo.dto.GeocodeResult;

public interface GeocodingService {

	GeocodeResult getCoordinates(String address);

	String buildAddressString(AccommodationRequest.AddressInfo addressInfo);
}
