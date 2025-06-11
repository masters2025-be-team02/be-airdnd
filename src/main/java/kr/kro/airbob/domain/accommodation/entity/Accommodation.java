package kr.kro.airbob.domain.accommodation.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import kr.kro.airbob.common.BaseEntity;
import kr.kro.airbob.domain.accommodation.common.AccommodationType;
import kr.kro.airbob.domain.member.Member;
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
public class Accommodation extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String description;

	private Integer basePrice;

	private String thumbnailUrl;

	@Enumerated(EnumType.STRING)
	private AccommodationType type;

	@OneToOne(fetch = FetchType.LAZY)
	private Address address;

	@OneToOne(fetch = FetchType.LAZY)
	private OccupancyPolicy occupancyPolicy;

	@OneToOne(fetch = FetchType.LAZY)
	private Member member;
}
