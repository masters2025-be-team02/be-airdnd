package kr.kro.airbob.domain.recentlyViewed;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import kr.kro.airbob.domain.accommodation.common.AmenityType;
import kr.kro.airbob.domain.accommodation.dto.AccommodationResponse;
import kr.kro.airbob.domain.common.BaseControllerDocumentationTest;

@WebMvcTest(RecentlyViewedController.class)
@DisplayName("위시리스트 관리 API 테스트")
class RecentlyViewedControllerTest extends BaseControllerDocumentationTest {

	@MockitoBean
	private RecentlyViewedService recentlyViewedService;

	@Autowired
	private RecentlyViewedController recentlyViewedController;
	@Override
	protected Object getController() {
		return recentlyViewedController;
	}

	@Test
	@DisplayName("숙소를 최근 조회 내역에 추가할 수 있다.")
	void addRecentlyViewed() throws Exception {
		// given
		Long memberId = 1L;
		Long accommodationId = 100L;

		willDoNothing().given(recentlyViewedService).addRecentlyViewed(memberId, accommodationId);

		// when & then
		mockMvc.perform(post("/api/members/recentlyViewed/{accommodationId}", accommodationId)
				.requestAttr("memberId", memberId))
			.andExpect(status().isOk())
			.andDo(document("recently-viewed-add",
				pathParameters(
					parameterWithName("accommodationId").description("숙소 ID")
				)));
	}

	@Test
	@DisplayName("최근 조회 내역에서 숙소를 삭제할 수 있다.")
	void removeRecentlyViewed() throws Exception {
		// given
		Long memberId = 1L;
		Long accommodationId = 100L;

		willDoNothing().given(recentlyViewedService).removeRecentlyViewed(memberId, accommodationId);

		// when & then
		mockMvc.perform(delete("/api/members/recentlyViewed/{accommodationId}", accommodationId)
				.requestAttr("memberId", memberId))
			.andExpect(status().isOk())
			.andDo(document("recently-viewed-remove",
				pathParameters(
					parameterWithName("accommodationId").description("삭제할 숙소 ID")
				)));
	}

	@Test
	@DisplayName("사용자의 최근 조회 내역을 조회할 수 있다.")
	void getRecentlyViewed() throws Exception {
		// given
		Long memberId = 1L;
		LocalDateTime now = LocalDateTime.now();

		AccommodationResponse.RecentlyViewedAccommodation accommodation1 =
			AccommodationResponse.RecentlyViewedAccommodation.builder()
				.accommodationId(100L)
				.accommodationName("서울 중심가 아늑한 원룸")
				.thumbnailUrl("https://example.com/image1.jpg")
				.viewedAt(now.minusHours(1))
				.averageRating(new BigDecimal("4.5"))
				.isInWishlist(true)
				.amenities(List.of(
					new AccommodationResponse.AmenityInfoResponse(AmenityType.WIFI, 1),
					new AccommodationResponse.AmenityInfoResponse(AmenityType.PARKING, 1)
				))
				.build();

		AccommodationResponse.RecentlyViewedAccommodation accommodation2 =
			AccommodationResponse.RecentlyViewedAccommodation.builder()
				.accommodationId(101L)
				.accommodationName("강남역 도보 5분 스튜디오")
				.thumbnailUrl("https://example.com/image2.jpg")
				.viewedAt(now.minusHours(3))
				.averageRating(new BigDecimal("4.2"))
				.isInWishlist(false)
				.amenities(List.of(
					new AccommodationResponse.AmenityInfoResponse(AmenityType.KITCHEN, 1)
				))
				.build();

		AccommodationResponse.RecentlyViewedAccommodations response =
			AccommodationResponse.RecentlyViewedAccommodations.builder()
				.accommodations(List.of(accommodation1, accommodation2))
				.totalCount(2)
				.build();

		given(recentlyViewedService.getRecentlyViewed(memberId)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/members/recentlyViewed")
				.requestAttr("memberId", memberId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accommodations").isArray())
			.andExpect(jsonPath("$.accommodations").value(org.hamcrest.Matchers.hasSize(2)))
			.andExpect(jsonPath("$.totalCount").value(2))
			.andExpect(jsonPath("$.accommodations[0].accommodationId").value(100))
			.andExpect(jsonPath("$.accommodations[0].accommodationName").value("서울 중심가 아늑한 원룸"))
			.andExpect(jsonPath("$.accommodations[0].thumbnailUrl").value("https://example.com/image1.jpg"))
			.andExpect(jsonPath("$.accommodations[0].averageRating").value(4.5))
			.andExpect(jsonPath("$.accommodations[0].isInWishlist").value(true))
			.andExpect(jsonPath("$.accommodations[0].amenities").isArray())
			.andExpect(jsonPath("$.accommodations[0].amenities").value(org.hamcrest.Matchers.hasSize(2)))
			.andExpect(jsonPath("$.accommodations[1].accommodationId").value(101))
			.andExpect(jsonPath("$.accommodations[1].isInWishlist").value(false))
			.andDo(document("recently-viewed-get",
				responseFields(
					fieldWithPath("accommodations").type(JsonFieldType.ARRAY)
						.description("최근 조회한 숙소 목록"),
					fieldWithPath("accommodations[].accommodationId").type(JsonFieldType.NUMBER)
						.description("숙소 ID"),
					fieldWithPath("accommodations[].accommodationName").type(JsonFieldType.STRING)
						.description("숙소 이름"),
					fieldWithPath("accommodations[].thumbnailUrl").type(JsonFieldType.STRING)
						.description("숙소 썸네일 이미지 URL"),
					fieldWithPath("accommodations[].viewedAt").type(JsonFieldType.ARRAY)
						.description("조회 시각"),
					fieldWithPath("accommodations[].averageRating").type(JsonFieldType.NUMBER)
						.description("평균 평점"),
					fieldWithPath("accommodations[].isInWishlist").type(JsonFieldType.BOOLEAN)
						.description("위시리스트 포함 여부"),
					fieldWithPath("accommodations[].amenities").type(JsonFieldType.ARRAY)
						.description("숙소 편의시설 목록"),
					fieldWithPath("accommodations[].amenities[].type").type(JsonFieldType.STRING)
						.description("편의시설 타입"),
					fieldWithPath("accommodations[].amenities[].count").type(JsonFieldType.NUMBER)
						.description("편의시설 개수"),
					fieldWithPath("totalCount").type(JsonFieldType.NUMBER)
						.description("전체 최근 조회 숙소 개수")
				)));
	}

	@Test
	@DisplayName("최근 조회 내역이 비어있을 때 빈 목록을 반환한다.")
	void getRecentlyViewed_Empty() throws Exception {
		// given
		Long memberId = 1L;

		AccommodationResponse.RecentlyViewedAccommodations emptyResponse =
			AccommodationResponse.RecentlyViewedAccommodations.builder()
				.accommodations(List.of())
				.totalCount(0)
				.build();

		given(recentlyViewedService.getRecentlyViewed(memberId)).willReturn(emptyResponse);

		// when & then
		mockMvc.perform(get("/api/members/recentlyViewed")
				.requestAttr("memberId", memberId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accommodations").isArray())
			.andExpect(jsonPath("$.accommodations").isEmpty())
			.andExpect(jsonPath("$.totalCount").value(0));
	}
}
