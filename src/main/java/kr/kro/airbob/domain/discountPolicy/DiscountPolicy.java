package kr.kro.airbob.domain.discountPolicy;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.kro.airbob.common.BaseEntity;
import kr.kro.airbob.domain.discountPolicy.common.DiscountType;
import kr.kro.airbob.domain.discountPolicy.common.PromotionType;
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

	private String name;
	private Double discountRate;
	private String description;
	@Enumerated(EnumType.STRING)
	private DiscountType discountType;
	@Enumerated(EnumType.STRING)
	private PromotionType promotionType;
	private Integer minPaymentPrice;
	private Integer maxApplyPrice;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private Boolean isActive;
}
