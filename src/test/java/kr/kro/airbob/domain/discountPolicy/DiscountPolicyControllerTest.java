package kr.kro.airbob.domain.discountPolicy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import kr.kro.airbob.cursor.util.CursorDecoder;
import kr.kro.airbob.domain.discountPolicy.entity.DiscountPolicy;
import kr.kro.airbob.domain.accommodation.interceptor.AccommodationAuthorizationInterceptor;
import kr.kro.airbob.domain.discountPolicy.common.DiscountType;
import kr.kro.airbob.domain.discountPolicy.common.PromotionType;
import kr.kro.airbob.domain.discountPolicy.dto.request.DiscountPolicyCreateDto;
import kr.kro.airbob.domain.discountPolicy.dto.request.DiscountPolicyUpdateDto;
import kr.kro.airbob.domain.discountPolicy.dto.response.DiscountPolicyResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@WebMvcTest(DiscountPolicyController.class)
public class DiscountPolicyControllerTest {

    @MockitoBean
    private DiscountPolicyService discountPolicyService;
    @MockitoBean
    private CursorDecoder cursorDecoder;
    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;
    @MockitoBean
    private AccommodationAuthorizationInterceptor accommodationAuthorizationInterceptor;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("등록된 할인 정책이 조회되어야 한다.")
    void findDiscountPolicies() throws Exception {
        //given
        DiscountPolicy discountPolicy = DiscountPolicy.builder()
                .id(1L)
                .name("신규회원 할인")
                .discountRate(0.15)
                .description("테스트 할인입니다.")
                .discountType(DiscountType.PERCENTAGE)
                .promotionType(PromotionType.COUPON)
                .minPaymentPrice(30000)
                .maxApplyPrice(10000)
                .startDate(LocalDateTime.of(2025, 6, 1, 0, 0))
                .endDate(LocalDateTime.of(2025, 6, 30, 23, 59))
                .isActive(true)
                .build();

        DiscountPolicyResponseDto discountPolicyResponse = DiscountPolicyResponseDto.of(discountPolicy);
        List<DiscountPolicyResponseDto> response = List.of(discountPolicyResponse);

        given(discountPolicyService.findValidDiscountPolicies()).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/discount")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("find-valid-discount-policies",
                        responseFields(
                                fieldWithPath("[].name").type(JsonNodeType.STRING).description("할인 정책 이름"),
                                fieldWithPath("[].discountRate").type(JsonNodeType.NUMBER).description("할인율"),
                                fieldWithPath("[].promotionType").type(JsonNodeType.STRING).description("프로모션 유형"),
                                fieldWithPath("[].minPaymentPrice").type(JsonNodeType.NUMBER).description("최소 결제 금액"),
                                fieldWithPath("[].maxApplyPrice").type(JsonNodeType.NUMBER).description("최대 할인 금액"),
                                fieldWithPath("[].startDate").type(JsonNodeType.STRING).description("시작일시"),
                                fieldWithPath("[].endDate").type(JsonNodeType.STRING).description("종료일시")
                        )));
    }

    @Test
    @DisplayName("할인 정책 정보를 입력 받아서 할인 정책을 등록해야한다.")
    void createDiscountPolicy() throws Exception {
        //given
        DiscountPolicyCreateDto request = DiscountPolicyCreateDto.builder()
                .name("신규회원 할인")
                .discountRate(0.15)
                .description("15% 할인 이벤트")
                .discountType(DiscountType.PERCENTAGE)
                .promotionType(PromotionType.COUPON)
                .minPaymentPrice(30000)
                .maxApplyPrice(10000)
                .startDate(LocalDateTime.of(2025, 6, 1, 0, 0))
                .endDate(LocalDateTime.of(2025, 6, 30, 23, 59))
                .isActive(true)
                .build();

        // when & then
        mockMvc.perform(post("/api/discount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("create-discount-policy",
                        requestFields(
                                fieldWithPath("name").type(JsonNodeType.STRING).description("할인 정책 이름"),
                                fieldWithPath("discountRate").type(JsonNodeType.NUMBER).description("할인율 (0.15 = 15%)"),
                                fieldWithPath("description").type(JsonNodeType.STRING).description("할인 정책 설명"),
                                fieldWithPath("discountType").type(JsonNodeType.STRING).description("할인 유형 (e.g. PERCENTAGE, FIXED)"),
                                fieldWithPath("promotionType").type(JsonNodeType.STRING).description("프로모션 유형 (e.g. COUPON, EVENT 등)"),
                                fieldWithPath("minPaymentPrice").type(JsonNodeType.NUMBER).description("할인 적용 최소 결제 금액"),
                                fieldWithPath("maxApplyPrice").type(JsonNodeType.NUMBER).description("최대 할인 적용 금액"),
                                fieldWithPath("startDate").type(JsonNodeType.STRING).description("할인 시작일시"),
                                fieldWithPath("endDate").type(JsonNodeType.STRING).description("할인 종료일시"),
                                fieldWithPath("isActive").type(JsonNodeType.BOOLEAN).description("활성화 여부")
                        )
                ));
    }

    @Test
    @DisplayName("할인 정책이 수정되어야 한다.")
    void updateDiscountPolicy() throws Exception {
        // given
        Long discountPolicyId = 1L;

        DiscountPolicyUpdateDto updateDto = DiscountPolicyUpdateDto.builder()
                .name("리뉴얼 할인")
                .discountRate(0.2)
                .description("2025년 여름 한정 할인")
                .discountType(DiscountType.PERCENTAGE)
                .promotionType(PromotionType.COUPON)
                .minPaymentPrice(30000)
                .maxApplyPrice(5000)
                .startDate(LocalDateTime.of(2025, 6, 1, 0, 0))
                .endDate(LocalDateTime.of(2025, 6, 30, 23, 59))
                .isActive(true)
                .build();

        String request = objectMapper.writeValueAsString(updateDto);

        // when & then
        mockMvc.perform(patch("/api/discount/{discountPolicyId}", discountPolicyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("update-discount-policy",
                        pathParameters(
                                parameterWithName("discountPolicyId").description("수정할 할인 정책 ID")
                        ),
                        requestFields(
                                fieldWithPath("name").type(JsonNodeType.STRING).description("할인 정책 이름"),
                                fieldWithPath("discountRate").type(JsonNodeType.NUMBER).description("할인율"),
                                fieldWithPath("description").type(JsonNodeType.STRING).description("설명"),
                                fieldWithPath("discountType").type(JsonNodeType.STRING).description("할인 타입"),
                                fieldWithPath("promotionType").type(JsonNodeType.STRING).description("프로모션 유형"),
                                fieldWithPath("minPaymentPrice").type(JsonNodeType.NUMBER).description("최소 결제 금액"),
                                fieldWithPath("maxApplyPrice").type(JsonNodeType.NUMBER).description("최대 할인 금액"),
                                fieldWithPath("startDate").type(JsonNodeType.STRING).description("시작일시"),
                                fieldWithPath("endDate").type(JsonNodeType.STRING).description("종료일시"),
                                fieldWithPath("isActive").type(JsonNodeType.BOOLEAN).description("활성화 여부")
                        )
                ));
    }

    @Test
    @DisplayName("할인 정책이 삭제되어야 한다.")
    void deleteDiscountPolicy() throws Exception {
        // given
        Long discountPolicyId = 1L;

        // discountpolicyService.deletePolicy(discountPolicyId) 메서드 호출을 검증만 하고 실제 동작은 하지 않도록 설정
        willDoNothing().given(discountPolicyService).deletePolicy(discountPolicyId);

        // when & then
        mockMvc.perform(delete("/api/discount/{discountPolicyId}", discountPolicyId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andDo(document("delete-discount-policy",
                        pathParameters(
                                parameterWithName("discountPolicyId").description("삭제할 할인 정책 ID")
                        )
                ));
    }
}
