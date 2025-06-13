package kr.kro.airbob.domain.discountPolicy.dto.response;

import kr.kro.airbob.domain.discountPolicy.DiscountPolicy;
import kr.kro.airbob.domain.discountPolicy.common.PromotionType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DiscountPolicyResponseDto {

    private String name;
    private Double discountRate;
    private PromotionType promotionType;
    private Integer minPaymentPrice;
    private Integer maxApplyPrice;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public static DiscountPolicyResponseDto of(DiscountPolicy discountPolicy) {
        return new DiscountPolicyResponseDto(
                discountPolicy.getName(),
                discountPolicy.getDiscountRate(),
                discountPolicy.getPromotionType(),
                discountPolicy.getMinPaymentPrice(),
                discountPolicy.getMaxApplyPrice(),
                discountPolicy.getStartDate(),
                discountPolicy.getEndDate());
    }

}
