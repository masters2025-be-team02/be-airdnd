package kr.kro.airbob.domain.accommodation.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.kro.airbob.common.BaseEntity;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest;
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
	private Integer postalCode;
	private Double latitude;
	private Double longitude;

	public static Address createAddress(AccommodationRequest.AddressInfo addressInfo) {
		// todo 위도, 경도 계산 로직 추가
		return Address.builder()
				.country(addressInfo.getCountry())
				.city(addressInfo.getCity())
				.district(addressInfo.getDistrict())
				.street(addressInfo.getStreet())
				.detail(addressInfo.getDetail())
				.postalCode(addressInfo.getPostalCode())
				.build();
	}
}
