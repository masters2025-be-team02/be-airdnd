package kr.kro.airbob.domain.discountPolicy.service;

import kr.kro.airbob.domain.discountPolicy.DiscountPolicy;
import kr.kro.airbob.domain.discountPolicy.dto.request.DiscountPolicyCreateDto;
import kr.kro.airbob.domain.discountPolicy.dto.request.DiscountPolicyUpdateDto;
import kr.kro.airbob.domain.discountPolicy.dto.response.DiscountPolicyResponseDto;
import kr.kro.airbob.domain.discountPolicy.repository.DiscountPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscountPolicyService {

    private final DiscountPolicyRepository discountPolicyRepository;

    @Transactional(readOnly = true)
    public List<DiscountPolicyResponseDto> findValidDiscountPolicies() {
        return discountPolicyRepository.findAll().stream()
                .filter(DiscountPolicy::getIsActive)
                .map(DiscountPolicyResponseDto::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createDiscountPolicy(DiscountPolicyCreateDto discountPolicyCreateDto) {
        DiscountPolicy discountPolicy = DiscountPolicy.of(discountPolicyCreateDto);

        discountPolicyRepository.save(discountPolicy);
    }

    @Transactional
    public void updateDiscountPolicy(DiscountPolicyUpdateDto discountPolicyUpdateDto, Long discountPolicyId) {
        DiscountPolicy discountPolicy = discountPolicyRepository.findById(discountPolicyId)
                .orElseThrow(() -> new NoSuchElementException("해당 id에 해당하는 할인 정책이 없습니다."));

        discountPolicy.updateWithDto(discountPolicyUpdateDto);
    }

    @Transactional
    public void deletePolicy(Long discountPolicyId) {
        DiscountPolicy discountPolicy = discountPolicyRepository.findById(discountPolicyId)
                .orElseThrow(() -> new NoSuchElementException("해당 id에 해당하는 할인 정책이 없습니다."));

        discountPolicyRepository.delete(discountPolicy);
    }
}
