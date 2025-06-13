package kr.kro.airbob.domain.discountPolicy;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import kr.kro.airbob.common.BaseEntity;
import kr.kro.airbob.domain.discountPolicy.common.DiscountType;
import kr.kro.airbob.domain.discountPolicy.common.PromotionType;
import kr.kro.airbob.domain.discountPolicy.dto.request.DiscountPolicyCreateDto;
import kr.kro.airbob.domain.discountPolicy.dto.request.DiscountPolicyUpdateDto;
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
public class DiscountPolicy extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Double discountRate;

	private String description;

	@Enumerated(EnumType.STRING)
	private DiscountType discountType;

	@Enumerated(EnumType.STRING)
	private PromotionType promotionType;

	private Integer minPaymentPrice;

	private Integer maxApplyPrice;

	@Column(nullable = false)
	private LocalDateTime startDate;

	@Column(nullable = false)
	private LocalDateTime endDate;

	@Column(nullable = false)
	private Boolean isActive;

	public static DiscountPolicy of(DiscountPolicyCreateDto discountPolicyCreateDto) {
        return DiscountPolicy.builder()
                .name(discountPolicyCreateDto.getName())
                .discountRate(discountPolicyCreateDto.getDiscountRate())
                .description(discountPolicyCreateDto.getDescription())
                .discountType(discountPolicyCreateDto.getDiscountType())
                .promotionType(discountPolicyCreateDto.getPromotionType())
                .minPaymentPrice(discountPolicyCreateDto.getMinPaymentPrice())
                .maxApplyPrice(discountPolicyCreateDto.getMaxApplyPrice())
                .startDate(discountPolicyCreateDto.getStartDate())
                .endDate(discountPolicyCreateDto.getEndDate())
                .isActive(discountPolicyCreateDto.getIsActive()).build();
	}

	public void updateWithDto(DiscountPolicyUpdateDto dto) {
		if (dto.getName() != null) this.name = dto.getName();
		if (dto.getDiscountRate() != null) this.discountRate = dto.getDiscountRate();
		if (dto.getDescription() != null) this.description = dto.getDescription();
		if (dto.getDiscountType() != null) this.discountType = dto.getDiscountType();
		if (dto.getPromotionType() != null) this.promotionType = dto.getPromotionType();
		if (dto.getMinPaymentPrice() != null) this.minPaymentPrice = dto.getMinPaymentPrice();
		if (dto.getMaxApplyPrice() != null) this.maxApplyPrice = dto.getMaxApplyPrice();
		if (dto.getStartDate() != null) this.startDate = dto.getStartDate();
		if (dto.getEndDate() != null) this.endDate = dto.getEndDate();
		if (dto.getIsActive() != null) this.isActive = dto.getIsActive();
	}

}
