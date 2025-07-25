package kr.kro.airbob.domain.discountPolicy;

import kr.kro.airbob.domain.discountPolicy.dto.request.DiscountPolicyCreateDto;
import kr.kro.airbob.domain.discountPolicy.dto.request.DiscountPolicyUpdateDto;
import kr.kro.airbob.domain.discountPolicy.dto.response.DiscountPolicyResponseDto;
import kr.kro.airbob.domain.discountPolicy.entity.DiscountPolicy;
import kr.kro.airbob.domain.discountPolicy.exception.DiscountNotFoundException;
import kr.kro.airbob.domain.discountPolicy.repository.DiscountPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountPolicyService {

    private final DiscountPolicyRepository discountPolicyRepository;

    @Transactional(readOnly = true)
    public List<DiscountPolicyResponseDto> findValidDiscountPolicies() {
        return new ArrayList<>(discountPolicyRepository.findActiveDiscountPolicies());
    }

    @Transactional
    public void createDiscountPolicy(DiscountPolicyCreateDto discountPolicyCreateDto) {
        DiscountPolicy discountPolicy = DiscountPolicy.of(discountPolicyCreateDto);

        discountPolicyRepository.save(discountPolicy);
    }

    @Transactional
    public void updateDiscountPolicy(DiscountPolicyUpdateDto discountPolicyUpdateDto, Long discountPolicyId) {
        DiscountPolicy discountPolicy = discountPolicyRepository.findById(discountPolicyId)
                .orElseThrow(DiscountNotFoundException::new);

        discountPolicy.updateWithDto(discountPolicyUpdateDto);
    }

    @Transactional
    public void deletePolicy(Long discountPolicyId) {
        DiscountPolicy discountPolicy = discountPolicyRepository.findById(discountPolicyId)
                .orElseThrow(DiscountNotFoundException::new);

        discountPolicyRepository.delete(discountPolicy);
    }
}
