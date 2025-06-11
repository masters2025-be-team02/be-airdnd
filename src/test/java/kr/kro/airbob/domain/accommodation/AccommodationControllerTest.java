package kr.kro.airbob.domain.accommodation;

import static com.fasterxml.jackson.databind.node.JsonNodeType.ARRAY;
import static com.fasterxml.jackson.databind.node.JsonNodeType.NUMBER;
import static com.fasterxml.jackson.databind.node.JsonNodeType.STRING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import kr.kro.airbob.domain.accommodation.common.AccommodationType;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest.AddressInfo;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest.AmenityInfo;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest.CreateAccommodationDto;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest.OccupancyPolicyInfo;
import kr.kro.airbob.domain.accommodation.entity.Accommodation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureRestDocs
@WebMvcTest(AccommodationController.class)
class AccommodationControllerTest {

    @MockitoBean
    private AccommodationService accommodationService;

    @Autowired
    private MockMvc mockMvc;

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
                .type(AccommodationType.HOUSE)
                .addressInfo(AddressInfo.builder()
                        .postalCode(12345)
                        .city("서울")
                        .country("대한민국")
                        .detail("상세주소")
                        .district("강남구")
                        .street("테헤란로")
                        .build())
                .amenityInfos(List.of(
                        AmenityInfo.builder()
                                .name("bed")
                                .count(2)
                                .build(),
                        AmenityInfo.builder()
                                .name("parking")
                                .count(1)
                                .build()))
                .occupancyPolicyInfo(OccupancyPolicyInfo.builder()
                        .maxOccupancy(6)
                        .adultOccupancy(4)
                        .childOccupancy(1)
                        .infantOccupancy(1)
                        .petOccupancy(0)
                        .build())
                .build();

        given(accommodationService.createAccommodation(any()))
                .willReturn(Accommodation.builder().id(1L).build());

        // when & then
        mockMvc.perform(post("/api/accommodations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("register-accommodation",
                        requestFields(
                                fieldWithPath("name").type(STRING).description("숙소 이름"),
                                fieldWithPath("description").type(STRING).description("숙소 설명"),
                                fieldWithPath("basePrice").type(NUMBER).description("기본 가격"),
                                fieldWithPath("hostId").type(NUMBER).description("호스트 ID"),
                                fieldWithPath("thumbnail_url").type(STRING).description("대표 썸네일 URL"),
                                fieldWithPath("type").type(STRING).description("숙소 타입 (예: HOUSE, APARTMENT 등)"),

                                // addressInfo
                                fieldWithPath("addressInfo.postalCode").type(NUMBER).description("우편번호"),
                                fieldWithPath("addressInfo.city").type(STRING).description("도시"),
                                fieldWithPath("addressInfo.country").type(STRING).description("국가"),
                                fieldWithPath("addressInfo.detail").type(STRING).description("상세 주소"),
                                fieldWithPath("addressInfo.district").type(STRING).description("구/군"),
                                fieldWithPath("addressInfo.street").type(STRING).description("도로명 주소"),

                                // amenityInfos
                                fieldWithPath("amenityInfos").type(ARRAY).description("어메니티 리스트"),
                                fieldWithPath("amenityInfos[].name").type(STRING).description("어메니티 이름"),
                                fieldWithPath("amenityInfos[].count").type(NUMBER).description("어메니티 수량"),

                                // occupancyPolicyInfo
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
}
