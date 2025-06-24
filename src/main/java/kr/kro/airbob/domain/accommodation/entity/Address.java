package kr.kro.airbob.domain.accommodation.entity;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.kro.airbob.common.domain.BaseEntity;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest;
import kr.kro.airbob.geo.dto.GeocodeResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String country;
	private String city;
	private String district;
	private String street;
	private String detail;
	private String postalCode;
	private Double latitude;
	private Double longitude;

	public static Address createAddress(AccommodationRequest.AddressInfo addressInfo, GeocodeResult geocodeResult) {
		return Address.builder()
			.country(addressInfo.getCountry())
			.city(addressInfo.getCity())
			.district(addressInfo.getDistrict())
			.street(addressInfo.getStreet())
			.detail(addressInfo.getDetail())
			.postalCode(addressInfo.getPostalCode())
			.latitude(geocodeResult.success() ? geocodeResult.latitude() : null)
			.longitude(geocodeResult.success() ? geocodeResult.longitude() : null)
			.build();
	}

	public boolean isChanged(AccommodationRequest.AddressInfo newAddressInfo) {
		return !Objects.equals(this.country, newAddressInfo.getCountry()) ||
			!Objects.equals(this.city, newAddressInfo.getCity()) ||
			!Objects.equals(this.district, newAddressInfo.getDistrict()) ||
			!Objects.equals(this.street, newAddressInfo.getStreet()) ||
			!Objects.equals(this.detail, newAddressInfo.getDetail()) ||
			!Objects.equals(this.postalCode, newAddressInfo.getPostalCode());
	}
}
