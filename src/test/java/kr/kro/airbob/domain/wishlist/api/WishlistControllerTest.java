package kr.kro.airbob.domain.wishlist.api;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import kr.kro.airbob.domain.common.api.BaseControllerDocumentationTest;
import kr.kro.airbob.domain.wishlist.WishlistService;
import kr.kro.airbob.domain.wishlist.dto.WishlistRequest;
import kr.kro.airbob.domain.wishlist.dto.WishlistResponse;

@WebMvcTest(WishlistController.class)
@DisplayName("위시리스트 관리 API 테스트")
class WishlistControllerTest extends BaseControllerDocumentationTest {

	@MockitoBean
	private WishlistService wishlistService;

	@Autowired
	private WishlistController wishlistController;

	@Override
	protected Object getController() {
		return wishlistController;
	}

	@Test
	@DisplayName("시나리오: 사용자가 새로운 위시리스트를 생성한다")
	void 사용자가_새로운_위시리스트를_생성한다() throws Exception {
		// Given: 사용자가 위시리스트 생성을 위한 유효한 데이터를 입력한 상황
		WishlistRequest.createRequest request = new WishlistRequest.createRequest("서울 여행 계획");
		WishlistResponse.createResponse expectedResponse = new WishlistResponse.createResponse(1L);

		when(wishlistService.createWishlist(any(WishlistRequest.createRequest.class), eq(1L)))
			.thenReturn(expectedResponse);

		// When: 사용자가 위시리스트 생성 API를 호출한다
		mockMvc.perform(post("/api/members/wishlists")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))

			// Then: 위시리스트가 성공적으로 생성되고 생성된 위시리스트 정보가 반환된다
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L))

			// document
			.andDo(document("위시리스트-생성-성공",
				requestFields(
					fieldWithPath("name")
						.type(JsonFieldType.STRING)
						.description("생성할 위시리스트의 이름")
				),
				responseFields(
					fieldWithPath("id")
						.type(JsonFieldType.NUMBER)
						.description("생성된 위시리스트의 고유 식별자")
				)));

		verify(wishlistService).createWishlist(any(WishlistRequest.createRequest.class), eq(1L));
	}

	@Test
	@DisplayName("시나리오: 사용자가 빈 이름으로 위시리스트 생성을 시도한다")
	void 사용자가_빈_이름으로_위시리스트_생성을_시도한다() throws Exception {
		// Given: 사용자가 빈 이름으로 위시리스트를 생성하려는 상황
		WishlistRequest.createRequest invalidRequest = new WishlistRequest.createRequest("");

		// When: 사용자가 빈 이름으로 위시리스트 생성을 시도한다
		mockMvc.perform(post("/api/members/wishlists")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidRequest)))

			// Then: 유효성 검증 오류가 발생한다.
			.andExpect(status().isBadRequest())

			// document
			.andDo(document("위시리스트-생성-빈이름-실패",
				requestFields(
					fieldWithPath("name")
						.type(JsonFieldType.STRING)
						.description("빈 문자열 (유효하지 않은 입력)")
				)));
	}

	@Test
	@DisplayName("시나리오: 사용자가 공백 문자 이름으로 위시리스트 생성을 시도한다")
	void 사용자가_공백_문자_이름으로_위시리스트_생성을_시도한다() throws Exception {
		// Given: 사용자가 공백 문자 이름으로 위시리스트를 생성하려는 상황
		WishlistRequest.createRequest invalidRequest = new WishlistRequest.createRequest(" ");

		// When: 사용자가 공백 문자 이름으로 위시리스트 생성을 시도한다
		mockMvc.perform(post("/api/members/wishlists")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidRequest)))

			// Then: 유효성 검증 오류가 발생한다.
			.andExpect(status().isBadRequest())

			// document
			.andDo(document("위시리스트-생성-빈이름-실패",
				requestFields(
					fieldWithPath("name")
						.type(JsonFieldType.STRING)
						.description("공백 문자 문자열 (유효하지 않은 입력)")
				)));
	}

	@Test
	@DisplayName("시나리오: 사용자가 이름을 입력하지 않고 위시리스트 생성을 시도한다")
	void 사용자가_이름을_입력하지_않고_위시리스트_생성을_시도한다() throws Exception {
		// Given: 사용자가 이름을 입력하지 않고 위시리스트를 생성하려는 상황 - null
		WishlistRequest.createRequest invalidRequest = new WishlistRequest.createRequest(null);

		// When: 사용자가 이름을 입력하지 않고 위시리스트를 생성을 시도한다
		mockMvc.perform(post("/api/members/wishlists")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidRequest)))

			// Then: 유효성 검증 오류가 발생한다.
			.andExpect(status().isBadRequest())

			// document
			.andDo(document("위시리스트-생성-빈이름-실패",
				requestFields(
					fieldWithPath("name")
						.type(JsonFieldType.NULL)
						.description("NULL (유효하지 않은 입력)")
				)));
	}

	@Test
	@DisplayName("시나리오: 사용자가 255자를 초과하는 이름으로 위시리스트 생성을 시도한다")
	void 사용자가_255자_초과_이름으로_위시리스트_생성을_시도한다() throws Exception {
		// Given: 255자를 초과하는 긴 이름으로 위시리스트를 생성하려는 상황
		String longName = "A".repeat(256);
		WishlistRequest.createRequest invalidRequest = new WishlistRequest.createRequest(longName);

		// When: 사용자가 긴 이름으로 위시리스트 생성을 시도한다
		mockMvc.perform(post("/api/members/wishlists")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidRequest)))

			// Then: 유효성 검증 오류가 발생한다
			.andExpect(status().isBadRequest())

			// document
			.andDo(document("위시리스트-생성-길이초과-실패",
				requestFields(
					fieldWithPath("name")
						.type(JsonFieldType.STRING)
						.description("255자를 초과하는 이름 (유효하지 않은 입력)")
				)
			));
	}

	@Test
	@DisplayName("시나리오: 여러 개의 유효한 위시리스트를 연속으로 생성한다")
	void 여러_개의_유효한_위시리스트를_연속으로_생성한다() throws Exception {
		// Given: 여러 개의 유요한 위시리스트 이름들
		String[] wishlistNames = {"부산 여행", "청주 여행", "세종 여행", "대전 여행", "속초 여행"};

		for (int i = 0; i < wishlistNames.length; i++) {
			WishlistRequest.createRequest request = new WishlistRequest.createRequest(wishlistNames[i]);
			WishlistResponse.createResponse expectedResponse = new WishlistResponse.createResponse((long)(i + 1));

			when(wishlistService.createWishlist(any(WishlistRequest.createRequest.class), eq(1L)))
				.thenReturn(expectedResponse);

			// When: 각각의 위시리스트를 생성한다
			mockMvc.perform(post("/api/members/wishlists")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))

				// Then: 모든 위시리스트가 성공적으로 생성된다
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(i + 1))

				// document
				.andDo(document("위시리스트-연속생성-" + (i + 1),
					requestFields(
						fieldWithPath("name")
							.type(JsonFieldType.STRING)
							.description("위시리스트 이름: " + wishlistNames[i])
					),
					responseFields(
						fieldWithPath("id")
							.type(JsonFieldType.NUMBER)
							.description("생성된 위시리스트 ID")
					)));
		}
	}

	@Test
	@DisplayName("시나리오: 동일한 이름으로 여러 위시리스트를 생성한다")
	void 동일한_이름으로_여러_위시리스트를_생성한다() throws Exception{
		// Given: 동일한 이름으로 위시리스트를 생성하는 상황 - 중복 허용
		WishlistRequest.createRequest request = new WishlistRequest.createRequest("초복중복말복");
		WishlistResponse.createResponse firstResponse = new WishlistResponse.createResponse(1L);
		WishlistResponse.createResponse secondResponse = new WishlistResponse.createResponse(2L);

		// 첫 번째 생성
		when(wishlistService.createWishlist(any(WishlistRequest.createRequest.class), eq(1L)))
			.thenReturn(firstResponse);

		mockMvc.perform(post("/api/members/wishlists")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L));

		// 두 번째 생성
		when(wishlistService.createWishlist(any(WishlistRequest.createRequest.class), eq(1L)))
			.thenReturn(secondResponse);

		mockMvc.perform(post("/api/members/wishlists")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))

			// Then: 중복 이름이여도 새로운 위시리스트가 생성된다
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(2L))

			// document
			.andDo(document("위시리스트-중복이름-허용",
				requestFields(
					fieldWithPath("name")
						.type(JsonFieldType.STRING)
						.description("위시리스트 이름 (중복 허용)")
				),
				responseFields(
					fieldWithPath("id")
						.type(JsonFieldType.NUMBER)
						.description("새로 생성된 위시리스트의 고유 ID")
				)));
	}
}
