package kr.kro.airbob.domain.discountPolicy.repository;

import kr.kro.airbob.domain.discountPolicy.entity.DiscountPolicy;
import kr.kro.airbob.domain.discountPolicy.dto.response.DiscountPolicyResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscountPolicyRepository extends JpaRepository<DiscountPolicy, Long> {

    @Query("SELECT new kr.kro.airbob.domain.discountPolicy.dto.response.DiscountPolicyResponseDto(" +
            "d.name, d.discountRate, d.promotionType, " +
            "d.minPaymentPrice, d.maxApplyPrice, d.startDate, d.endDate) " +
            "FROM DiscountPolicy d " +
            "WHERE d.isActive = true")
    List<DiscountPolicyResponseDto> findActiveDiscountPolicies();

}
