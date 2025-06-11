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
public class OccupancyPolicy extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Integer maxOccupancy;
	private Integer adultOccupancy;
	private Integer childOccupancy;
	private Integer infantOccupancy;
	private Integer petOccupancy;

	public static OccupancyPolicy createOccupancyPolicy(AccommodationRequest.OccupancyPolicyInfo occupancyPolicyInfo) {
		return OccupancyPolicy.builder()
				.maxOccupancy(occupancyPolicyInfo.getMaxOccupancy())
				.adultOccupancy(occupancyPolicyInfo.getAdultOccupancy())
				.childOccupancy(occupancyPolicyInfo.getChildOccupancy())
				.infantOccupancy(occupancyPolicyInfo.getInfantOccupancy())
				.petOccupancy(occupancyPolicyInfo.getPetOccupancy())
				.build();
	}
}
