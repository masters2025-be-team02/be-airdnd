package kr.kro.airbob.domain.discountPolicy.repository;

import kr.kro.airbob.domain.discountPolicy.DiscountPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountPolicyRepository extends JpaRepository<DiscountPolicy, Long> {
}
