package kr.kro.airbob.domain.discountPolicy.dto.request;

import kr.kro.airbob.domain.discountPolicy.common.DiscountType;
import kr.kro.airbob.domain.discountPolicy.common.PromotionType;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class DiscountPolicyCreateDto {

    private String name;
    private Double discountRate;
    private String description;
    private DiscountType discountType;
    private PromotionType promotionType;
    private Integer minPaymentPrice;
    private Integer maxApplyPrice;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;

}
