package kr.kro.airbob.domain.discountPolicy;

import kr.kro.airbob.domain.discountPolicy.entity.DiscountPolicy;
import kr.kro.airbob.domain.discountPolicy.common.DiscountType;
import kr.kro.airbob.domain.discountPolicy.common.PromotionType;
import kr.kro.airbob.domain.discountPolicy.dto.request.DiscountPolicyCreateDto;
import kr.kro.airbob.domain.discountPolicy.dto.request.DiscountPolicyUpdateDto;
import kr.kro.airbob.domain.discountPolicy.dto.response.DiscountPolicyResponseDto;
import kr.kro.airbob.domain.discountPolicy.repository.DiscountPolicyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DiscountPolicyServiceTest {

    @InjectMocks
    private DiscountPolicyService discountPolicyService;

    @Mock
    private DiscountPolicyRepository discountPolicyRepository;

    @Test
    @DisplayName("DB에 있는 할인 정책들이 조회되어야 한다.")
    void findValidDiscountPoliciesSuccess() {
        // given
        DiscountPolicyResponseDto dto1 = new DiscountPolicyResponseDto(
                "테스트 할인1",
                10.0,
                PromotionType.EARLY_BIRD,
                10000,
                10000,
                LocalDateTime.of(2025, 6, 1, 0, 0),
                LocalDateTime.of(2025, 6, 30, 23, 59)
        );

        DiscountPolicyResponseDto dto2 = new DiscountPolicyResponseDto(
                "테스트 할인2",
                20.0,
                PromotionType.LONG_STAY,
                30000,
                5000,
                LocalDateTime.of(2025, 6, 1, 0, 0),
                LocalDateTime.of(2025, 6, 30, 23, 59)
        );

        given(discountPolicyRepository.findActiveDiscountPolicies())
                .willReturn(List.of(dto1, dto2));

        // when
        List<DiscountPolicyResponseDto> result = discountPolicyService.findValidDiscountPolicies();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactlyInAnyOrder("테스트 할인1", "테스트 할인2");
    }

    @Test
    @DisplayName("정상적인 정보를 받으면 할인 정책이 정상적으로 저장되어야 한다.")
    void createDiscountPolicyVerifySaveCall() {
        // given
        DiscountPolicyCreateDto createDto = DiscountPolicyCreateDto.builder()
                .name("10% 할인")
                .discountRate(10.0)
                .description("전 상품 10% 할인")
                .discountType(DiscountType.PERCENTAGE)
                .promotionType(PromotionType.COUPON)
                .minPaymentPrice(10000)
                .maxApplyPrice(20000)
                .startDate(LocalDateTime.of(2025, 6, 1, 0, 0))
                .endDate(LocalDateTime.of(2025, 6, 30, 23, 59))
                .isActive(true)
                .build();

        // when
        discountPolicyService.createDiscountPolicy(createDto);

        // then
        ArgumentCaptor<DiscountPolicy> captor = ArgumentCaptor.forClass(DiscountPolicy.class);
        verify(discountPolicyRepository, times(1)).save(captor.capture());

        DiscountPolicy captured = captor.getValue();

        assertThat(captured.getName()).isEqualTo("10% 할인");
        assertThat(captured.getDiscountRate()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("정상적인 정보를 받으면 할인 정책 수정에 성공해야 한다.")
    void updateDiscountPolicySuccess() {
        // given
        Long policyId = 1L;

        DiscountPolicyUpdateDto updateDto = DiscountPolicyUpdateDto.builder()
                .name("20% 할인")
                .discountRate(20.0)
                .description("새로운 설명")
                .discountType(DiscountType.PERCENTAGE)
                .promotionType(PromotionType.COUPON)
                .minPaymentPrice(5000)
                .maxApplyPrice(15000)
                .startDate(LocalDateTime.of(2025, 7, 1, 0, 0))
                .endDate(LocalDateTime.of(2025, 7, 31, 23, 59))
                .isActive(true)
                .build();

        DiscountPolicy discountPolicy = DiscountPolicy.builder()
                .name("10% 할인")
                .discountRate(10.0)
                .description("기존 설명")
                .discountType(DiscountType.PERCENTAGE)
                .promotionType(PromotionType.COUPON)
                .minPaymentPrice(10000)
                .maxApplyPrice(20000)
                .startDate(LocalDateTime.of(2025, 6, 1, 0, 0))
                .endDate(LocalDateTime.of(2025, 6, 30, 23, 59))
                .isActive(true)
                .build();

        given(discountPolicyRepository.findById(policyId)).willReturn(Optional.of(discountPolicy));

        // when
        discountPolicyService.updateDiscountPolicy(updateDto, policyId);

        // then
        assertThat(discountPolicy.getName()).isEqualTo("20% 할인");
        assertThat(discountPolicy.getDiscountRate()).isEqualTo(20.0);
        assertThat(discountPolicy.getDescription()).isEqualTo("새로운 설명");
        assertThat(discountPolicy.getDiscountType()).isEqualTo(DiscountType.PERCENTAGE);
        assertThat(discountPolicy.getPromotionType()).isEqualTo(PromotionType.COUPON);
        assertThat(discountPolicy.getMinPaymentPrice()).isEqualTo(5000);
        assertThat(discountPolicy.getMaxApplyPrice()).isEqualTo(15000);
        assertThat(discountPolicy.getStartDate()).isEqualTo(LocalDateTime.of(2025, 7, 1, 0, 0));
        assertThat(discountPolicy.getEndDate()).isEqualTo(LocalDateTime.of(2025, 7, 31, 23, 59));
        assertThat(discountPolicy.getIsActive()).isTrue();

        verify(discountPolicyRepository).findById(policyId);
    }


    @Test
    @DisplayName("존재하지 않는 할인 정책 수정을 수정하면 예외가 발생해야 한다.")
    void updateDiscountPolicyNotFound() {
        // given
        Long policyId = 999L;
        DiscountPolicyUpdateDto updateDto = DiscountPolicyUpdateDto.builder()
                .name("20% 할인")
                .discountRate(20.0)
                .description("새로운 설명")
                .discountType(DiscountType.PERCENTAGE)
                .promotionType(PromotionType.COUPON)
                .minPaymentPrice(5000)
                .maxApplyPrice(15000)
                .startDate(LocalDateTime.of(2025, 7, 1, 0, 0))
                .endDate(LocalDateTime.of(2025, 7, 31, 23, 59))
                .isActive(true)
                .build();

        given(discountPolicyRepository.findById(policyId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> discountPolicyService.updateDiscountPolicy(updateDto, policyId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("해당 id에 해당하는 할인 정책이 없습니다.");
    }

    @Test
    @DisplayName("할인 정책 삭제 성공")
    void deletePolicySuccess() {
        // given
        Long policyId = 1L;
        DiscountPolicy discountPolicy = mock(DiscountPolicy.class);

        given(discountPolicyRepository.findById(policyId)).willReturn(Optional.of(discountPolicy));

        ArgumentCaptor<DiscountPolicy> captor = ArgumentCaptor.forClass(DiscountPolicy.class);

        // when
        discountPolicyService.deletePolicy(policyId);

        // then
        verify(discountPolicyRepository, times(1)).delete(captor.capture());
        assertThat(captor.getValue()).isEqualTo(discountPolicy); // 삭제된 객체가 맞는지 확인
    }

    @Test
    @DisplayName("존재하지 않는 할인 정책 삭제 시 예외가 발생해야한다.")
    void deletePolicyNotFound() {
        // given
        Long policyId = 999L;
        given(discountPolicyRepository.findById(policyId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> discountPolicyService.deletePolicy(policyId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("해당 id에 해당하는 할인 정책이 없습니다.");
    }

}
