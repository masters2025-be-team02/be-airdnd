package kr.kro.airbob.domain.accommodation;

import static com.fasterxml.jackson.databind.node.JsonNodeType.ARRAY;
import static com.fasterxml.jackson.databind.node.JsonNodeType.NUMBER;
import static com.fasterxml.jackson.databind.node.JsonNodeType.OBJECT;
import static com.fasterxml.jackson.databind.node.JsonNodeType.STRING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.restdocs.RestDocumentationExtension;

@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureRestDocs
@WebMvcTest(AccommodationController.class)
class AccommodationControllerTest {

    @MockitoBean
    private AccommodationService accommodationService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation)
                        .operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint())
                        .and()
                        .uris()
                        .withScheme("https")
                        .withHost("api.airbob.kro.kr")
                        .withPort(443))
                .build();
    }

    @Test
    @DisplayName("정보를 입력받아서 숙소를 등록한다")
    void createAccommodation() throws Exception {
        // given
        CreateAccommodationDto request = CreateAccommodationDto.builder()
                .name("테스트 숙소")
                .description("좋은 숙소입니다.")
                .basePrice(50000)
                .hostId(1L)
                .thumbnail_url("http://example.com/image.jpg")
                .type("VILLA")
                .addressInfo(AddressInfo.builder()
                        .postalCode(12345)
                        .city("서울")
                        .country("대한민국")
                        .detail("상세주소")
                        .district("강남구")
                        .street("테헤란로")
                        .build())
                .amenityInfos(List.of(
                        AmenityInfo.builder().name("bed_linens").count(2).build(),
                        AmenityInfo.builder().name("parking").count(1).build()
                ))
                .occupancyPolicyInfo(OccupancyPolicyInfo.builder()
                        .maxOccupancy(6)
                        .adultOccupancy(4)
                        .childOccupancy(1)
                        .infantOccupancy(1)
                        .petOccupancy(0)
                        .build())
                .build();

        given(accommodationService.createAccommodation(any()))
                .willReturn(1L);

        // when & then
        mockMvc.perform(post("/api/accommodations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("숙소 등록",
                        requestFields(
                                fieldWithPath("name").type(STRING).description("숙소 이름"),
                                fieldWithPath("description").type(STRING).description("숙소 설명"),
                                fieldWithPath("basePrice").type(NUMBER).description("기본 가격"),
                                fieldWithPath("hostId").type(NUMBER).description("호스트 ID"),
                                fieldWithPath("thumbnail_url").type(STRING).description("대표 썸네일 URL"),
                                fieldWithPath("type").type(STRING).description("숙소 타입 (예: HOUSE, APARTMENT 등)"),

                                fieldWithPath("addressInfo.postalCode").type(NUMBER).description("우편번호"),
                                fieldWithPath("addressInfo.city").type(STRING).description("도시"),
                                fieldWithPath("addressInfo.country").type(STRING).description("국가"),
                                fieldWithPath("addressInfo.detail").type(STRING).description("상세 주소"),
                                fieldWithPath("addressInfo.district").type(STRING).description("구/군"),
                                fieldWithPath("addressInfo.street").type(STRING).description("도로명 주소"),

                                fieldWithPath("amenityInfos").type(ARRAY).description("어메니티 리스트"),
                                fieldWithPath("amenityInfos[].name").type(STRING).description("어메니티 이름"),
                                fieldWithPath("amenityInfos[].count").type(NUMBER).description("어메니티 수량"),

                                fieldWithPath("occupancyPolicyInfo.maxOccupancy").type(NUMBER).description("최대 수용 인원"),
                                fieldWithPath("occupancyPolicyInfo.adultOccupancy").type(NUMBER).description("성인 인원"),
                                fieldWithPath("occupancyPolicyInfo.childOccupancy").type(NUMBER).description("아동 인원"),
                                fieldWithPath("occupancyPolicyInfo.infantOccupancy").type(NUMBER).description("유아 인원"),
                                fieldWithPath("occupancyPolicyInfo.petOccupancy").type(NUMBER).description("반려동물 인원")
                        ),
                        responseFields(
                                fieldWithPath("id").type(NUMBER).description("생성된 숙소 ID")
                        )
                ));
    }

    @Test
    @DisplayName("입력받은 정보로 숙소를 수정한다")
    void updateAccommodationTest() throws Exception {
        Long accommodationId = 1L;

        UpdateAccommodationDto request = UpdateAccommodationDto.builder()
                .name("테스트 수정 숙소")
                .description("나쁘지 않은 숙소입니다.")
                .basePrice(500000)
                .type("VILLA")
                .addressInfo(AddressInfo.builder()
                        .postalCode(33333)
                        .city("서울")
                        .country("대한민국")
                        .detail("상세주소")
                        .district("성북구")
                        .street("테스트로")
                        .build())
                .amenityInfos(List.of(
                        AmenityInfo.builder().name("bed_linens").count(2).build(),
                        AmenityInfo.builder().name("parking").count(1).build()
                ))
                .occupancyPolicyInfo(OccupancyPolicyInfo.builder()
                        .maxOccupancy(6)
                        .adultOccupancy(4)
                        .childOccupancy(1)
                        .infantOccupancy(1)
                        .petOccupancy(0)
                        .build())
                .build();

        mockMvc.perform(patch("/api/accommodations/{accommodationId}", accommodationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("숙소 정보 수정",
                        pathParameters(
                                parameterWithName("accommodationId").description("수정할 숙소의 ID")
                        ),
                        requestFields(
                                fieldWithPath("name").type(STRING).description("숙소 이름").optional(),
                                fieldWithPath("description").type(STRING).description("숙소 설명").optional(),
                                fieldWithPath("basePrice").type(NUMBER).description("기본 가격").optional(),
                                fieldWithPath("type").type(STRING).description("숙소 타입 (예: HOTEL, GUESTHOUSE, MOTEL 등)").optional(),

                                fieldWithPath("addressInfo").type(OBJECT).description("주소 정보").optional(),
                                fieldWithPath("addressInfo.postalCode").type(NUMBER).description("우편번호").optional(),
                                fieldWithPath("addressInfo.city").type(STRING).description("도시").optional(),
                                fieldWithPath("addressInfo.country").type(STRING).description("국가").optional(),
                                fieldWithPath("addressInfo.district").type(STRING).description("구/군").optional(),
                                fieldWithPath("addressInfo.street").type(STRING).description("거리").optional(),
                                fieldWithPath("addressInfo.detail").type(STRING).description("상세 주소").optional(),

                                fieldWithPath("occupancyPolicyInfo").type(OBJECT).description("숙박 정책 정보").optional(),
                                fieldWithPath("occupancyPolicyInfo.maxOccupancy").type(NUMBER).description("최대 수용 인원").optional(),
                                fieldWithPath("occupancyPolicyInfo.adultOccupancy").type(NUMBER).description("성인 수용 인원").optional(),
                                fieldWithPath("occupancyPolicyInfo.childOccupancy").type(NUMBER).description("어린이 수용 인원").optional(),
                                fieldWithPath("occupancyPolicyInfo.infantOccupancy").type(NUMBER).description("유아 수용 인원").optional(),
                                fieldWithPath("occupancyPolicyInfo.petOccupancy").type(NUMBER).description("반려동물 수용 인원").optional(),

                                fieldWithPath("amenityInfos").type(ARRAY).description("편의 시설 리스트").optional(),
                                fieldWithPath("amenityInfos[].name").type(STRING).description("편의 시설 이름").optional(),
                                fieldWithPath("amenityInfos[].count").type(NUMBER).description("편의 시설 개수").optional()
                        )
                ));
    }

    @Test
    @DisplayName("숙소를 삭제한다")
    void deleteAccommodation() throws Exception {
        // given
        Long accommodationId = 1L;

        // when & then
        mockMvc.perform(delete("/api/accommodations/{accommodationId}", accommodationId))
                .andExpect(status().isNoContent())
                .andDo(document("숙소 삭제",
                        pathParameters(
                                parameterWithName("accommodationId").description("삭제할 숙소의 ID")
                        )
                ));
    }
}
