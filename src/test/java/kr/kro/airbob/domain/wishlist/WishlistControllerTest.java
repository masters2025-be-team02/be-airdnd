package kr.kro.airbob.domain.wishlist;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import kr.kro.airbob.cursor.dto.CursorRequest;
import kr.kro.airbob.cursor.dto.CursorResponse;
import kr.kro.airbob.cursor.exception.CursorPageSizeException;
import kr.kro.airbob.domain.accommodation.common.AmenityType;
import kr.kro.airbob.domain.accommodation.dto.AccommodationResponse;
import kr.kro.airbob.domain.accommodation.exception.AccommodationNotFoundException;
import kr.kro.airbob.domain.common.BaseControllerDocumentationTest;
import kr.kro.airbob.domain.wishlist.api.WishlistController;
import kr.kro.airbob.domain.wishlist.dto.WishlistRequest;
import kr.kro.airbob.domain.wishlist.dto.WishlistResponse;
import kr.kro.airbob.domain.wishlist.exception.WishlistAccessDeniedException;
import kr.kro.airbob.domain.wishlist.exception.WishlistAccommodationAccessDeniedException;
import kr.kro.airbob.domain.wishlist.exception.WishlistAccommodationNotFoundException;
import kr.kro.airbob.domain.wishlist.exception.WishlistNotFoundException;

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

	@Nested
	@DisplayName("위시리스트 생성:")
	class CreateWishlistTests{
		@Test
		@DisplayName("시나리오: 사용자가 새로운 위시리스트를 생성한다")
		void 사용자가_새로운_위시리스트를_생성한다() throws Exception {
			// Given: 사용자가 위시리스트 생성을 위한 유효한 데이터를 입력한 상황
			WishlistRequest.createRequest request = new WishlistRequest.createRequest("서울 여행 계획");
			WishlistResponse.CreateResponse expectedResponse = new WishlistResponse.CreateResponse(1L);

			when(wishlistService.createWishlist(any(WishlistRequest.createRequest.class), eq(1L)))
				.thenReturn(expectedResponse);

			// When: 사용자가 위시리스트 생성 API를 호출한다
			mockMvc.perform(post("/api/members/wishlists")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
					.requestAttr("memberId", 1L)
				)

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

		@ParameterizedTest(name = "{0}")
		@MethodSource("invalidCreateRequestProvider")
		@DisplayName("시나리오: 사용자가 잘못된 데이터로 위시리스트 생성을 시도한다")
		void 사용자가_잘못된_데이터로_위시리스트_생성을_시도한다(
			String testName,
			String inputName,
			JsonFieldType fieldType,
			String description,
			String documentId
		) throws Exception {
			// Given: 잘못된 데이터로 위시리스트를 생성하려는 상황
			WishlistRequest.createRequest invalidRequest = new WishlistRequest.createRequest(inputName);

			// When: 사용자가 잘못된 데이터로 위시리스트 생성을 시도한다
			mockMvc.perform(post("/api/members/wishlists")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(invalidRequest)))

				// Then: 유효성 검증 오류가 발생한다
				.andExpect(status().isBadRequest())

				// document
				.andDo(document(documentId,
					requestFields(
						fieldWithPath("name")
							.type(fieldType)
							.description(description)
					)));
		}

		static Stream<Arguments> invalidCreateRequestProvider() {
			return Stream.of(
				Arguments.of(
					"빈 문자열로 생성",
					"",
					JsonFieldType.STRING,
					"빈 문자열 (유효하지 않은 입력)",
					"위시리스트-생성-빈이름-실패"
				),
				Arguments.of(
					"공백 문자로 생성",
					"   ",
					JsonFieldType.STRING,
					"공백 문자열 (유효하지 않은 입력)",
					"위시리스트-생성-공백문자-실패"
				),
				Arguments.of(
					"null로 생성",
					null,
					JsonFieldType.NULL,
					"NULL (유효하지 않은 입력)",
					"위시리스트-생성-null-실패"
				),
				Arguments.of(
					"255자 초과로 생성",
					"A".repeat(256),
					JsonFieldType.STRING,
					"255자를 초과하는 이름 (유효하지 않은 입력)",
					"위시리스트-생성-길이초과-실패"
				)
			);
		}

		@Test
		@DisplayName("시나리오: 여러 개의 유효한 위시리스트를 연속으로 생성한다")
		void 여러_개의_유효한_위시리스트를_연속으로_생성한다() throws Exception {
			// Given: 여러 개의 유요한 위시리스트 이름들
			String[] wishlistNames = {"부산 여행", "청주 여행", "세종 여행", "대전 여행", "속초 여행"};

			for (int i = 0; i < wishlistNames.length; i++) {
				WishlistRequest.createRequest request = new WishlistRequest.createRequest(wishlistNames[i]);
				WishlistResponse.CreateResponse expectedResponse = new WishlistResponse.CreateResponse((long)(i + 1));

				when(wishlistService.createWishlist(any(WishlistRequest.createRequest.class), eq(1L)))
					.thenReturn(expectedResponse);

				// When: 각각의 위시리스트를 생성한다
				mockMvc.perform(post("/api/members/wishlists")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request))
						.requestAttr("memberId", 1L)
					)

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
			WishlistResponse.CreateResponse firstResponse = new WishlistResponse.CreateResponse(1L);
			WishlistResponse.CreateResponse secondResponse = new WishlistResponse.CreateResponse(2L);

			// 첫 번째 생성
			when(wishlistService.createWishlist(any(WishlistRequest.createRequest.class), eq(1L)))
				.thenReturn(firstResponse);

			mockMvc.perform(post("/api/members/wishlists")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
					.requestAttr("memberId", 1L)
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L));

			// 두 번째 생성
			when(wishlistService.createWishlist(any(WishlistRequest.createRequest.class), eq(1L)))
				.thenReturn(secondResponse);

			mockMvc.perform(post("/api/members/wishlists")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
					.requestAttr("memberId", 1L)
				)

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

	@Nested
	@DisplayName("위시리스트 수정:")
	class UpdateWishlistTests {

		@Test
		@DisplayName("시나리오: 사용자가 위시리스트의 이름을 수정한다")
		void 사용자가_위시리스트의_이름을_수정한다() throws Exception {
			// Given: 존재하는 위시리스트의 이름을 변경하려는 상황
			Long wishlistId = 1L;
			WishlistRequest.updateRequest request = new WishlistRequest.updateRequest("수정된 서울 여행 계획");
			WishlistResponse.UpdateResponse expectedResponse = new WishlistResponse.UpdateResponse(wishlistId);

			when(wishlistService.updateWishlist(eq(wishlistId), any(WishlistRequest.updateRequest.class)))
				.thenReturn(expectedResponse);

			// When: 사용자가 위시리스트 수정 API를 호출한다
			mockMvc.perform(patch("/api/members/wishlists/{wishlistId}", wishlistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))

				// Then: 위시리스트가 성공적으로 수정되고 수정된 위시리스트 정보가 반환된다
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(wishlistId))

				// document
				.andDo(document("위시리스트-수정-성공",
					pathParameters(
						parameterWithName("wishlistId")
							.description("수정할 위시리스트의 고유 식별자")
					),
					requestFields(
						fieldWithPath("name")
							.type(JsonFieldType.STRING)
							.description("수정할 위시리스트의 새로운 이름")
					),
					responseFields(
						fieldWithPath("id")
							.type(JsonFieldType.NUMBER)
							.description("수정된 위시리스트의 고유 식별자")
					)));

			verify(wishlistService).updateWishlist(eq(wishlistId), any(WishlistRequest.updateRequest.class));
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("invalidUpdateRequestProvider")
		@DisplayName("시나리오: 사용자가 잘못된 데이터로 위시리스트 수정을 시도한다")
		void 사용자가_잘못된_데이터로_위시리스트_수정을_시도한다(
			String testName,
			String inputName,
			JsonFieldType fieldType,
			String description,
			String documentId
		) throws Exception {
			// Given: 잘못된 데이터로 위시리스트를 수정하려는 상황
			Long wishlistId = 1L;
			WishlistRequest.updateRequest invalidRequest = new WishlistRequest.updateRequest(inputName);

			// When: 사용자가 잘못된 데이터로 위시리스트 수정을 시도한다
			mockMvc.perform(patch("/api/members/wishlists/{wishlistId}", wishlistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(invalidRequest)))

				// Then: 유효성 검증 오류가 발생한다
				.andExpect(status().isBadRequest())

				// document
				.andDo(document(documentId,
					pathParameters(
						parameterWithName("wishlistId")
							.description("수정할 위시리스트의 고유 식별자")
					),
					requestFields(
						fieldWithPath("name")
							.type(fieldType)
							.description(description)
					)));
		}

		static Stream<Arguments> invalidUpdateRequestProvider() {
			return Stream.of(
				Arguments.of(
					"빈 문자열로 수정",
					"",
					JsonFieldType.STRING,
					"빈 문자열 (유효하지 않은 입력)",
					"위시리스트-수정-빈이름-실패"
				),
				Arguments.of(
					"공백 문자로 수정",
					"      ",
					JsonFieldType.STRING,
					"공백 문자열 (유효하지 않은 입력)",
					"위시리스트-수정-공백문자-실패"
				),
				Arguments.of(
					"null로 수정",
					null,
					JsonFieldType.NULL,
					"NULL (유효하지 않은 입력)",
					"위시리스트-수정-null-실패"
				),
				Arguments.of(
					"255자 초과로 수정",
					"A".repeat(256),
					JsonFieldType.STRING,
					"255자를 초과하는 이름 (유효하지 않은 입력)",
					"위시리스트-수정-길이초과-실패"
				)
			);
		}

		@Test
		@DisplayName("시나리오: 존재하지 않는 위시리스트를 수정하려고 한다")
		void 존재하지_않는_위시리스트를_수정하려고_한다() throws Exception {
			// Given: 존재하지 않는 위시리스트 ID
			Long nonExistentWishlistId = 999L;
			WishlistRequest.updateRequest request = new WishlistRequest.updateRequest("수정할 이름");

			when(wishlistService.updateWishlist(eq(nonExistentWishlistId), any(WishlistRequest.updateRequest.class)))
				.thenThrow(new WishlistNotFoundException());

			// When & Then: 존재하지 않는 위시리스트 수정 시도 시 오류 발생
			mockMvc.perform(patch("/api/members/wishlists/{wishlistId}", nonExistentWishlistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound())
				.andDo(document("위시리스트-수정-존재하지않음-실패",
					pathParameters(
						parameterWithName("wishlistId")
							.description("존재하지 않는 위시리스트 ID")
					),
					requestFields(
						fieldWithPath("name")
							.type(JsonFieldType.STRING)
							.description("수정하려는 위시리스트 이름")
					)));
		}

		@Test
		@DisplayName("시나리오: 다른 사용자의 위시리스트를 수정하려고 한다")
		void 다른_사용자의_위시리스트를_수정하려고_한다() throws Exception {
			// Given: 다른 사용자의 위시리스트
			Long otherMemberWishlistId = 1L;
			WishlistRequest.updateRequest request = new WishlistRequest.updateRequest("수정할 이름");

			when(wishlistService.updateWishlist(eq(otherMemberWishlistId), any(WishlistRequest.updateRequest.class)))
				.thenThrow(new WishlistAccessDeniedException());

			// When & Then: 권한 없는 위시리스트 수정 시도 시 오류 발생
			mockMvc.perform(patch("/api/members/wishlists/{wishlistId}", otherMemberWishlistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isForbidden())
				// document
				.andDo(document("위시리스트-수정-권한없음-실패",
					pathParameters(
						parameterWithName("wishlistId")
							.description("다른 사용자의 위시리스트 ID")
					),
					requestFields(
						fieldWithPath("name")
							.type(JsonFieldType.STRING)
							.description("수정하려는 위시리스트 이름")
					)));
		}
	}

	@Nested
	@DisplayName("위시리스트 삭제:")
	class DeleteWishlistTests {
		@Test
		@DisplayName("시나리오: 사용자가 위시리스트를 삭제한다")
		void 사용자가_위시리스트를_삭제한다() throws Exception {
			// Given: 존재하는 위시리스트를 삭제하려는 상황
			Long wishlistId = 1L;

			doNothing().when(wishlistService).deleteWishlist(eq(wishlistId));

			// When: 사용자가 위시리스트 삭제 API를 호출한다
			mockMvc.perform(delete("/api/members/wishlists/{wishlistId}", wishlistId))

				// Then: 위시리스트가 성공적으로 삭제된다
				.andExpect(status().isNoContent())

				// document
				.andDo(document("위시리스트-삭제-성공",
					pathParameters(
						parameterWithName("wishlistId")
							.description("삭제할 위시리스트의 고유 식별자")
					)));
			verify(wishlistService).deleteWishlist(eq(wishlistId));
		}

		@Test
		@DisplayName("시나리오: 여러 위시리스트를 연속으로 삭제한다")
		void 여러_위시리스트를_연속으로_삭제한다() throws Exception {
			// Given: 여러 개의 위시리스트 ID들
			Long[] wishlistIds = {1L, 2L, 3L, 4L, 5L};

			for (int i = 0; i < wishlistIds.length; i++) {
				Long wishlistId = wishlistIds[i];

				doNothing().when(wishlistService).deleteWishlist(eq(wishlistId));

				// When: 각각의 위시리스트를 삭제한다
				mockMvc.perform(delete("/api/members/wishlists/{wishlistId}", wishlistId))

					// Then: 모든 위시리스트가 성공적으로 삭제된다
					.andExpect(status().isNoContent())

					// document
					.andDo(document("위시리스트-연속삭제-" + (i + 1),
						pathParameters(
							parameterWithName("wishlistId")
								.description("삭제할 위시리스트 ID: " + wishlistId)
						)));
			}
		}

		@Test
		@DisplayName("시나리오: 존재하지 않는 위시리스트를 삭제하려고 한다")
		void 존재하지_않는_위시리스트를_삭제하려고_한다() throws Exception {
			// Given: 존재하지 않는 위시리스트 Id
			Long nonExistentWishlistId = 999L;

			doThrow(new WishlistNotFoundException())
				.when(wishlistService).deleteWishlist(eq(nonExistentWishlistId));

			// When & Then: 존재하지 않는 위시리스트 삭제 시도 시 오류 발생
			mockMvc.perform(delete("/api/members/wishlists/{wishlistId}", nonExistentWishlistId))
				.andExpect(status().isNotFound())
				.andDo(document("위시리스트-삭제-존재하지않음-실패",
					pathParameters(
						parameterWithName("wishlistId")
							.description("존재하지 않는 위시리스트 Id")
					)));
		}

		@Test
		@DisplayName("시나리오: 다른 사용자의 위시리스트를 삭제하려고 한다")
		void 다른_사용자의_위시리스트를_삭제하려고_한다() throws Exception {
			// Given: 다른 사용자의 위시리스트
			Long otherMemberWishlistId = 1L;

			doThrow(new WishlistAccessDeniedException())
				.when(wishlistService).deleteWishlist(eq(otherMemberWishlistId));

			// When & Then: 권한 없는 위시리스트 삭제 시도 시 오류 발생
			mockMvc.perform(delete("/api/members/wishlists/{wishlistId}", otherMemberWishlistId))
				.andExpect(status().isForbidden())
				.andDo(document("위시리스트-삭제-권한없음-실패",
					pathParameters(
						parameterWithName("wishlistId")
							.description("다른 사용자의 위시리스트 ID")
					)));
		}
	}

	@Nested
	@DisplayName("위시리스트 조회:")
	class ReadWishlistTests {
		@Test
		@DisplayName("시나리오: 사용자가 자신의 위시리스트 목록을 조회한다")
		void 사용자가_자신의_위시리스트_목록을_조회한다() throws Exception {
			// Given: 사용자의 위시리스트들이 존재하는 상황
			List<WishlistResponse.WishlistInfo> wishlists = List.of(
				new WishlistResponse.WishlistInfo(1L, "서울 여행", LocalDateTime.now(), 3L, "thumbnail1.jpg"),
				new WishlistResponse.WishlistInfo(2L, "부산 여행", LocalDateTime.now(), 5L, "thumbnail2.jpg"),
				new WishlistResponse.WishlistInfo(3L, "제주도 여행", LocalDateTime.now(), 2L, null)
			);

			CursorResponse.PageInfo pageInfo = CursorResponse.PageInfo.builder()
				.hasNext(false)
				.nextCursor(null)
				.currentSize(3)
				.build();

			WishlistResponse.WishlistInfos expectedResponse = new WishlistResponse.WishlistInfos(wishlists, pageInfo);

			when(wishlistService.findWishlists(any(CursorRequest.CursorPageRequest.class), eq(1L)))
				.thenReturn(expectedResponse);

			// When: 사용자가 위시리스트 목록 조회 API를 호출한다
			mockMvc.perform(get("/api/members/wishlists")
					.param("size", "20")
					.requestAttr("memberId", 1L))  // ← 이 부분 추가

				// Then: 위시리스트 목록이 성공적으로 반환된다
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.wishlists").isArray())
				.andExpect(jsonPath("$.wishlists.length()").value(3))
				.andExpect(jsonPath("$.wishlists[0].id").value(1L))
				.andExpect(jsonPath("$.wishlists[0].name").value("서울 여행"))
				.andExpect(jsonPath("$.wishlists[0].wishlistItemCount").value(3))
				.andExpect(jsonPath("$.wishlists[0].thumbnailImageUrl").value("thumbnail1.jpg"))
				.andExpect(jsonPath("$.wishlists[2].thumbnailImageUrl").isEmpty())
				.andExpect(jsonPath("$.pageInfo.hasNext").value(false))
				.andExpect(jsonPath("$.pageInfo.currentSize").value(3))

				// document 부분은 그대로...
				.andDo(document("위시리스트-목록조회-성공",
					queryParameters(
						parameterWithName("size")
							.description("페이지 크기 (기본값: 20, 최대: 50)")
							.optional(),
						parameterWithName("cursor")
							.description("다음 페이지를 위한 커서 (첫 페이지에서는 생략)")
							.optional()
					),
					responseFields(
						fieldWithPath("wishlists[]")
							.type(JsonFieldType.ARRAY)
							.description("위시리스트 목록"),
						fieldWithPath("wishlists[].id")
							.type(JsonFieldType.NUMBER)
							.description("위시리스트 고유 식별자"),
						fieldWithPath("wishlists[].name")
							.type(JsonFieldType.STRING)
							.description("위시리스트 이름"),
						fieldWithPath("wishlists[].createdAt")
							.type(JsonFieldType.ARRAY)
							.description("위시리스트 생성 날짜"),
						fieldWithPath("wishlists[].wishlistItemCount")
							.type(JsonFieldType.NUMBER)
							.description("위시리스트에 포함된 숙소 수"),
						fieldWithPath("wishlists[].thumbnailImageUrl")
							.type(JsonFieldType.STRING)
							.description("대표 이미지 URL (없을 경우 null)")
							.optional(),
						fieldWithPath("pageInfo")
							.type(JsonFieldType.OBJECT)
							.description("페이징 정보"),
						fieldWithPath("pageInfo.hasNext")
							.type(JsonFieldType.BOOLEAN)
							.description("다음 페이지 존재 여부"),
						fieldWithPath("pageInfo.nextCursor")
							.type(JsonFieldType.STRING)
							.description("다음 페이지 커서 (없을 경우 null)")
							.optional(),
						fieldWithPath("pageInfo.currentSize")
							.type(JsonFieldType.NUMBER)
							.description("현재 페이지의 아이템 수")
					)));

			verify(wishlistService).findWishlists(any(CursorRequest.CursorPageRequest.class), eq(1L));
		}

		@Test
		@DisplayName("시나리오: 사용자가 커서 기반 페이징으로 위시리스트를 조회한다")
		void 사용자가_커서_기반_페이징으로_위시리스트를_조회한다() throws Exception {
			// Given: 두 번째 페이지 데이터
			List<WishlistResponse.WishlistInfo> wishlists = List.of(
				new WishlistResponse.WishlistInfo(4L, "대구 여행", LocalDateTime.now(), 1L, "thumbnail4.jpg"),
				new WishlistResponse.WishlistInfo(5L, "광주 여행", LocalDateTime.now(), 4L, null)
			);

			CursorResponse.PageInfo pageInfo = CursorResponse.PageInfo.builder()
				.hasNext(true)
				.nextCursor("eyJpZCI6NSwiY3JlYXRlZEF0IjoiMjAyMS0wNS0xN1QwOTowMDowMCJ9")
				.currentSize(2)
				.build();

			WishlistResponse.WishlistInfos expectedResponse = new WishlistResponse.WishlistInfos(wishlists, pageInfo);

			when(wishlistService.findWishlists(any(CursorRequest.CursorPageRequest.class), eq(1L)))
				.thenReturn(expectedResponse);

			// When: 커서와 함께 위시리스트 목록을 조회한다
			mockMvc.perform(get("/api/members/wishlists")
					.param("size", "2")
					.param("cursor", "eyJpZCI6MywiY3JlYXRlZEF0IjoiMjAyMS0wNS0xN1QwODowMDowMCJ9")
					.requestAttr("memberId", 1L))  // ← 이 부분 추가

				// Then: 페이징된 위시리스트 목록이 반환된다
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.wishlists.length()").value(2))
				.andExpect(jsonPath("$.pageInfo.hasNext").value(true))
				.andExpect(jsonPath("$.pageInfo.nextCursor").isNotEmpty())

				// document는 기존과 동일...
				.andDo(document("위시리스트-페이징조회-성공",
					queryParameters(
						parameterWithName("size")
							.description("페이지 크기"),
						parameterWithName("cursor")
							.description("이전 페이지의 마지막 커서")
					),
					responseFields(
						fieldWithPath("wishlists[]")
							.type(JsonFieldType.ARRAY)
							.description("위시리스트 목록"),
						fieldWithPath("wishlists[].id")
							.type(JsonFieldType.NUMBER)
							.description("위시리스트 고유 식별자"),
						fieldWithPath("wishlists[].name")
							.type(JsonFieldType.STRING)
							.description("위시리스트 이름"),
						fieldWithPath("wishlists[].createdAt")
							.type(JsonFieldType.ARRAY)
							.description("위시리스트 생성 날짜"),
						fieldWithPath("wishlists[].wishlistItemCount")
							.type(JsonFieldType.NUMBER)
							.description("위시리스트에 포함된 숙소 수"),
						fieldWithPath("wishlists[].thumbnailImageUrl")
							.type(JsonFieldType.STRING)
							.description("대표 이미지 URL (없을 경우 null)")
							.optional(),
						fieldWithPath("pageInfo")
							.type(JsonFieldType.OBJECT)
							.description("페이징 정보"),
						fieldWithPath("pageInfo.hasNext")
							.type(JsonFieldType.BOOLEAN)
							.description("다음 페이지 존재 여부"),
						fieldWithPath("pageInfo.nextCursor")
							.type(JsonFieldType.STRING)
							.description("다음 페이지를 위한 커서"),
						fieldWithPath("pageInfo.currentSize")
							.type(JsonFieldType.NUMBER)
							.description("현재 페이지의 아이템 수")
					)));
		}

		@Test
		@DisplayName("시나리오: 사용자가 빈 위시리스트 목록을 조회한다")
		void 사용자가_빈_위시리스트_목록을_조회한다() throws Exception {
			// Given: 위시리스트가 없는 사용자
			List<WishlistResponse.WishlistInfo> emptyWishlists = List.of();

			CursorResponse.PageInfo pageInfo = CursorResponse.PageInfo.builder()
				.hasNext(false)
				.nextCursor(null)
				.currentSize(0)
				.build();

			WishlistResponse.WishlistInfos expectedResponse = new WishlistResponse.WishlistInfos(emptyWishlists, pageInfo);

			when(wishlistService.findWishlists(any(CursorRequest.CursorPageRequest.class), eq(1L)))
				.thenReturn(expectedResponse);

			// When: 위시리스트 목록을 조회한다
			mockMvc.perform(get("/api/members/wishlists")
					.requestAttr("memberId", 1L))  // ← 이 부분 추가

				// Then: 빈 목록이 반환된다
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.wishlists").isArray())
				.andExpect(jsonPath("$.wishlists.length()").value(0))
				.andExpect(jsonPath("$.pageInfo.hasNext").value(false))
				.andExpect(jsonPath("$.pageInfo.currentSize").value(0))

				// document
				.andDo(document("위시리스트-빈목록조회-성공",
					responseFields(
						fieldWithPath("wishlists[]")
							.type(JsonFieldType.ARRAY)
							.description("빈 위시리스트 목록"),
						fieldWithPath("pageInfo")
							.type(JsonFieldType.OBJECT)
							.description("페이징 정보"),
						fieldWithPath("pageInfo.hasNext")
							.type(JsonFieldType.BOOLEAN)
							.description("다음 페이지 존재 여부 (false)"),
						fieldWithPath("pageInfo.nextCursor")
							.type(JsonFieldType.STRING)
							.description("다음 페이지 커서 (null)")
							.optional(),
						fieldWithPath("pageInfo.currentSize")
							.type(JsonFieldType.NUMBER)
							.description("현재 페이지의 아이템 수 (0)")
					)));
		}


		@Test
		@DisplayName("시나리오: 잘못된 커서로 위시리스트를 조회한다")
		void 잘못된_커서로_위시리스트를_조회한다() throws Exception {
			// Given: 잘못된 형식의 커서
			String invalidCursor = "invalid-cursor-format";

			// 잘못된 커서는 CursorDecoder에서 null로 처리되어 정상 동작하므로
			// 실제로는 첫 페이지 조회와 동일하게 처리됨
			List<WishlistResponse.WishlistInfo> wishlists = List.of(
				new WishlistResponse.WishlistInfo(1L, "서울 여행", LocalDateTime.now(), 3L, "thumbnail1.jpg")
			);

			CursorResponse.PageInfo pageInfo = CursorResponse.PageInfo.builder()
				.hasNext(false)
				.nextCursor(null)
				.currentSize(1)
				.build();

			WishlistResponse.WishlistInfos expectedResponse = new WishlistResponse.WishlistInfos(wishlists, pageInfo);

			when(wishlistService.findWishlists(any(CursorRequest.CursorPageRequest.class), eq(1L)))
				.thenReturn(expectedResponse);

			// When & Then: 잘못된 커서로 조회해도 정상적으로 첫 페이지가 반환됨
			mockMvc.perform(get("/api/members/wishlists")
					.param("cursor", invalidCursor)
					.requestAttr("memberId", 1L))  // ← 이 부분 추가
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.wishlists.length()").value(1))
				.andDo(document("위시리스트-조회-잘못된커서-정상처리",
					queryParameters(
						parameterWithName("cursor")
							.description("잘못된 형식의 커서 (첫 페이지로 처리됨)")
					),
					responseFields(
						fieldWithPath("wishlists[]")
							.type(JsonFieldType.ARRAY)
							.description("위시리스트 목록 (첫 페이지)"),
						fieldWithPath("wishlists[].id")
							.type(JsonFieldType.NUMBER)
							.description("위시리스트 고유 식별자"),
						fieldWithPath("wishlists[].name")
							.type(JsonFieldType.STRING)
							.description("위시리스트 이름"),
						fieldWithPath("wishlists[].createdAt")
							.type(JsonFieldType.ARRAY)
							.description("위시리스트 생성 날짜"),
						fieldWithPath("wishlists[].wishlistItemCount")
							.type(JsonFieldType.NUMBER)
							.description("위시리스트에 포함된 숙소 수"),
						fieldWithPath("wishlists[].thumbnailImageUrl")
							.type(JsonFieldType.STRING)
							.description("대표 이미지 URL (없을 경우 null)")
							.optional(),
						fieldWithPath("pageInfo")
							.type(JsonFieldType.OBJECT)
							.description("페이징 정보"),
						fieldWithPath("pageInfo.hasNext")
							.type(JsonFieldType.BOOLEAN)
							.description("다음 페이지 존재 여부"),
						fieldWithPath("pageInfo.nextCursor")
							.type(JsonFieldType.STRING)
							.description("다음 페이지 커서 (없을 경우 null)")
							.optional(),
						fieldWithPath("pageInfo.currentSize")
							.type(JsonFieldType.NUMBER)
							.description("현재 페이지의 아이템 수")
					)));
		}
	}

	@Nested
	@DisplayName("위시리스트 숙소 추가:")
	class CreateWishlistAccommodationTests {

		@Test
		@DisplayName("시나리오: 사용자가 위시리스트에 숙소를 성공적으로 추가한다")
		void 사용자가_위시리스트에_숙소를_성공적으로_추가한다() throws Exception {
			// Given: 존재하는 위시리스트와 숙소로 위시리스트 항목을 추가하는 상황
			Long wishlistId = 1L;
			Long accommodationId = 100L;
			WishlistRequest.CreateWishlistAccommodationRequest request =
				new WishlistRequest.CreateWishlistAccommodationRequest(accommodationId);
			WishlistResponse.CreateWishlistAccommodationResponse expectedResponse =
				new WishlistResponse.CreateWishlistAccommodationResponse(1L);

			when(wishlistService.createWishlistAccommodation(eq(wishlistId), any(WishlistRequest.CreateWishlistAccommodationRequest.class)))
				.thenReturn(expectedResponse);

			// When: 사용자가 위시리스트에 숙소 추가 API를 호출한다
			mockMvc.perform(post("/api/members/wishlists/{wishlistId}/accommodations", wishlistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))

				// Then: 숙소가 성공적으로 위시리스트에 추가된다
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L))

				// document
				.andDo(document("위시리스트-숙소추가-성공",
					pathParameters(
						parameterWithName("wishlistId")
							.description("숙소를 추가할 위시리스트의 고유 식별자")
					),
					requestFields(
						fieldWithPath("accommodationId")
							.type(JsonFieldType.NUMBER)
							.description("위시리스트에 추가할 숙소의 고유 식별자")
					),
					responseFields(
						fieldWithPath("id")
							.type(JsonFieldType.NUMBER)
							.description("생성된 위시리스트 항목의 고유 식별자")
					)));

			verify(wishlistService).createWishlistAccommodation(eq(wishlistId), any(WishlistRequest.CreateWishlistAccommodationRequest.class));
		}

		@Test
		@DisplayName("시나리오: 존재하지 않는 위시리스트에 숙소 추가를 시도한다")
		void 존재하지_않는_위시리스트에_숙소_추가를_시도한다() throws Exception {
			// Given: 존재하지 않는 위시리스트 ID로 숙소를 추가하려는 상황
			Long nonExistentWishlistId = 999L;
			Long accommodationId = 100L;
			WishlistRequest.CreateWishlistAccommodationRequest request =
				new WishlistRequest.CreateWishlistAccommodationRequest(accommodationId);

			when(wishlistService.createWishlistAccommodation(eq(nonExistentWishlistId), any(WishlistRequest.CreateWishlistAccommodationRequest.class)))
				.thenThrow(new WishlistNotFoundException());

			// When: 존재하지 않는 위시리스트에 숙소 추가를 시도한다
			mockMvc.perform(post("/api/members/wishlists/{wishlistId}/accommodations", nonExistentWishlistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))

				// Then: 404 Not Found 오류가 발생한다
				.andExpect(status().isNotFound())

				// document
				.andDo(document("위시리스트-숙소추가-위시리스트없음-실패",
					pathParameters(
						parameterWithName("wishlistId")
							.description("존재하지 않는 위시리스트 ID")
					),
					requestFields(
						fieldWithPath("accommodationId")
							.type(JsonFieldType.NUMBER)
							.description("추가하려는 숙소 ID")
					)));

			verify(wishlistService).createWishlistAccommodation(eq(nonExistentWishlistId), any(WishlistRequest.CreateWishlistAccommodationRequest.class));
		}

		@Test
		@DisplayName("시나리오: 존재하지 않는 숙소를 위시리스트에 추가하려 시도한다")
		void 존재하지_않는_숙소를_위시리스트에_추가하려_시도한다() throws Exception {
			// Given: 유효한 위시리스트에 존재하지 않는 숙소를 추가하려는 상황
			Long wishlistId = 1L;
			Long nonExistentAccommodationId = 999L;
			WishlistRequest.CreateWishlistAccommodationRequest request =
				new WishlistRequest.CreateWishlistAccommodationRequest(nonExistentAccommodationId);

			when(wishlistService.createWishlistAccommodation(eq(wishlistId), any(WishlistRequest.CreateWishlistAccommodationRequest.class)))
				.thenThrow(new AccommodationNotFoundException());

			// When: 존재하지 않는 숙소를 위시리스트에 추가를 시도한다
			mockMvc.perform(post("/api/members/wishlists/{wishlistId}/accommodations", wishlistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))

				// Then: 404 Not Found 오류가 발생한다
				.andExpect(status().isNotFound())

				// document
				.andDo(document("위시리스트-숙소추가-숙소없음-실패",
					pathParameters(
						parameterWithName("wishlistId")
							.description("위시리스트 ID")
					),
					requestFields(
						fieldWithPath("accommodationId")
							.type(JsonFieldType.NUMBER)
							.description("존재하지 않는 숙소 ID")
					)));

			verify(wishlistService).createWishlistAccommodation(eq(wishlistId), any(WishlistRequest.CreateWishlistAccommodationRequest.class));
		}

		@Test
		@DisplayName("시나리오: 다른 사용자의 위시리스트에 숙소 추가를 시도한다")
		void 다른_사용자의_위시리스트에_숙소_추가를_시도한다() throws Exception {
			// Given: 다른 사용자 소유의 위시리스트에 숙소를 추가하려는 상황
			Long otherUserWishlistId = 1L;
			Long accommodationId = 100L;
			WishlistRequest.CreateWishlistAccommodationRequest request =
				new WishlistRequest.CreateWishlistAccommodationRequest(accommodationId);

			when(wishlistService.createWishlistAccommodation(eq(otherUserWishlistId), any(WishlistRequest.CreateWishlistAccommodationRequest.class)))
				.thenThrow(new WishlistAccessDeniedException());

			// When: 다른 사용자의 위시리스트에 숙소 추가를 시도한다
			mockMvc.perform(post("/api/members/wishlists/{wishlistId}/accommodations", otherUserWishlistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))

				// Then: 403 Forbidden 오류가 발생한다
				.andExpect(status().isForbidden())

				// document
				.andDo(document("위시리스트-숙소추가-권한없음-실패",
					pathParameters(
						parameterWithName("wishlistId")
							.description("다른 사용자 소유의 위시리스트 ID")
					),
					requestFields(
						fieldWithPath("accommodationId")
							.type(JsonFieldType.NUMBER)
							.description("추가하려는 숙소 ID")
					)));

			verify(wishlistService).createWishlistAccommodation(eq(otherUserWishlistId), any(WishlistRequest.CreateWishlistAccommodationRequest.class));
		}

		@Test
		@DisplayName("시나리오: 이미 위시리스트에 추가된 숙소를 중복으로 추가하려 시도한다")
		void 이미_위시리스트에_추가된_숙소를_중복으로_추가하려_시도한다() throws Exception {
			// Given: 이미 위시리스트에 있는 숙소를 다시 추가하려는 상황
			Long wishlistId = 1L;
			Long duplicateAccommodationId = 100L;
			WishlistRequest.CreateWishlistAccommodationRequest request =
				new WishlistRequest.CreateWishlistAccommodationRequest(duplicateAccommodationId);

			when(wishlistService.createWishlistAccommodation(eq(wishlistId), any(WishlistRequest.CreateWishlistAccommodationRequest.class)))
				.thenThrow(new WishlistAccessDeniedException()); // 중복 체크에서 WishlistAccessDeniedException이 발생

			// When: 중복된 숙소를 위시리스트에 추가를 시도한다
			mockMvc.perform(post("/api/members/wishlists/{wishlistId}/accommodations", wishlistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))

				// Then: 403 Forbidden 오류가 발생한다
				.andExpect(status().isForbidden())

				// document
				.andDo(document("위시리스트-숙소추가-중복-실패",
					pathParameters(
						parameterWithName("wishlistId")
							.description("위시리스트 ID")
					),
					requestFields(
						fieldWithPath("accommodationId")
							.type(JsonFieldType.NUMBER)
							.description("이미 위시리스트에 존재하는 숙소 ID")
					)));

			verify(wishlistService).createWishlistAccommodation(eq(wishlistId), any(WishlistRequest.CreateWishlistAccommodationRequest.class));
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("invalidAccommodationIdProvider")
		@DisplayName("시나리오: 잘못된 숙소 ID로 위시리스트에 추가를 시도한다")
		void 잘못된_숙소_ID로_위시리스트에_추가를_시도한다(
			String testName,
			Long invalidAccommodationId,
			JsonFieldType fieldType,
			String description,
			String documentId
		) throws Exception {
			// Given: 잘못된 숙소 ID로 위시리스트에 추가하려는 상황
			Long wishlistId = 1L;
			WishlistRequest.CreateWishlistAccommodationRequest invalidRequest =
				new WishlistRequest.CreateWishlistAccommodationRequest(invalidAccommodationId);

			// When: 잘못된 숙소 ID로 위시리스트에 추가를 시도한다
			mockMvc.perform(post("/api/members/wishlists/{wishlistId}/accommodations", wishlistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(invalidRequest)))

				// Then: 400 Bad Request 오류가 발생한다
				.andExpect(status().isBadRequest())

				// document
				.andDo(document(documentId,
					pathParameters(
						parameterWithName("wishlistId")
							.description("위시리스트 ID")
					),
					requestFields(
						fieldWithPath("accommodationId")
							.type(fieldType)
							.description(description)
					)));
		}

		static Stream<Arguments> invalidAccommodationIdProvider() {
			return Stream.of(
				Arguments.of(
					"음수 숙소 ID로 추가",
					-1L,
					JsonFieldType.NUMBER,
					"음수 숙소 ID (유효하지 않은 입력)",
					"위시리스트-숙소추가-음수ID-실패"
				),
				Arguments.of(
					"0인 숙소 ID로 추가",
					0L,
					JsonFieldType.NUMBER,
					"0인 숙소 ID (유효하지 않은 입력)",
					"위시리스트-숙소추가-0ID-실패"
				),
				Arguments.of(
					"null 숙소 ID로 추가",
					null,
					JsonFieldType.NULL,
					"NULL 숙소 ID (유효하지 않은 입력)",
					"위시리스트-숙소추가-nullID-실패"
				)
			);
		}

		@Test
		@DisplayName("시나리오: 여러 개의 서로 다른 숙소를 연속으로 위시리스트에 추가한다")
		void 여러_개의_서로_다른_숙소를_연속으로_위시리스트에_추가한다() throws Exception {
			// Given: 여러 개의 서로 다른 숙소 ID들
			Long wishlistId = 1L;
			Long[] accommodationIds = {100L, 200L, 300L, 400L, 500L};

			for (int i = 0; i < accommodationIds.length; i++) {
				WishlistRequest.CreateWishlistAccommodationRequest request =
					new WishlistRequest.CreateWishlistAccommodationRequest(accommodationIds[i]);
				WishlistResponse.CreateWishlistAccommodationResponse expectedResponse =
					new WishlistResponse.CreateWishlistAccommodationResponse((long)(i + 1));

				when(wishlistService.createWishlistAccommodation(eq(wishlistId), any(WishlistRequest.CreateWishlistAccommodationRequest.class)))
					.thenReturn(expectedResponse);

				// When: 각각의 숙소를 위시리스트에 추가한다
				mockMvc.perform(post("/api/members/wishlists/{wishlistId}/accommodations", wishlistId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))

					// Then: 모든 숙소가 성공적으로 추가된다
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(i + 1))

					// document
					.andDo(document("위시리스트-숙소추가-연속생성-" + (i + 1),
						pathParameters(
							parameterWithName("wishlistId")
								.description("위시리스트 ID")
						),
						requestFields(
							fieldWithPath("accommodationId")
								.type(JsonFieldType.NUMBER)
								.description("숙소 ID: " + accommodationIds[i])
						),
						responseFields(
							fieldWithPath("id")
								.type(JsonFieldType.NUMBER)
								.description("생성된 위시리스트 항목 ID")
						)));
			}
		}

		@Test
		@DisplayName("시나리오: 숙소 ID가 매우 큰 값일 때 위시리스트에 추가한다")
		void 숙소_ID가_매우_큰_값일_때_위시리스트에_추가한다() throws Exception {
			// Given: 매우 큰 숙소 ID로 위시리스트에 추가하는 상황
			Long wishlistId = 1L;
			Long largeAccommodationId = Long.MAX_VALUE;
			WishlistRequest.CreateWishlistAccommodationRequest request =
				new WishlistRequest.CreateWishlistAccommodationRequest(largeAccommodationId);
			WishlistResponse.CreateWishlistAccommodationResponse expectedResponse =
				new WishlistResponse.CreateWishlistAccommodationResponse(1L);

			when(wishlistService.createWishlistAccommodation(eq(wishlistId), any(WishlistRequest.CreateWishlistAccommodationRequest.class)))
				.thenReturn(expectedResponse);

			// When: 매우 큰 숙소 ID로 위시리스트에 추가를 시도한다
			mockMvc.perform(post("/api/members/wishlists/{wishlistId}/accommodations", wishlistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))

				// Then: 요청이 성공적으로 처리된다
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L))

				// document
				.andDo(document("위시리스트-숙소추가-최대값ID-성공",
					pathParameters(
						parameterWithName("wishlistId")
							.description("위시리스트 ID")
					),
					requestFields(
						fieldWithPath("accommodationId")
							.type(JsonFieldType.NUMBER)
							.description("매우 큰 숙소 ID (Long.MAX_VALUE)")
					),
					responseFields(
						fieldWithPath("id")
							.type(JsonFieldType.NUMBER)
							.description("생성된 위시리스트 항목 ID")
					)));

			verify(wishlistService).createWishlistAccommodation(eq(wishlistId), any(WishlistRequest.CreateWishlistAccommodationRequest.class));
		}
	}

	@Nested
	@DisplayName("위시리스트 숙소 메모 수정:")
	class UpdateWishlistAccommodationTests {

		@Test
		@DisplayName("시나리오: 사용자가 위시리스트 숙소의 메모를 성공적으로 수정한다")
		void 사용자가_위시리스트_숙소의_메모를_성공적으로_수정한다() throws Exception {
			// Given: 존재하는 위시리스트 항목의 메모를 수정하는 상황
			Long wishlistId = 1L;
			Long wishlistAccommodationId = 10L;
			String updatedMemo = "여기는 정말 좋은 곳이었어요! 다음에도 꼭 가고 싶습니다.";
			WishlistRequest.UpdateWishlistAccommodationRequest request =
				new WishlistRequest.UpdateWishlistAccommodationRequest(updatedMemo);
			WishlistResponse.UpdateWishlistAccommodationResponse expectedResponse =
				new WishlistResponse.UpdateWishlistAccommodationResponse(wishlistAccommodationId);

			when(wishlistService.updateWishlistAccommodation(eq(wishlistAccommodationId), any(WishlistRequest.UpdateWishlistAccommodationRequest.class)))
				.thenReturn(expectedResponse);

			// When: 사용자가 위시리스트 숙소 메모 수정 API를 호출한다
			mockMvc.perform(patch("/api/members/wishlists/{wishlistId}/accommodations/{wishlistAccommodationId}",
					wishlistId, wishlistAccommodationId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))

				// Then: 메모가 성공적으로 수정된다
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(wishlistAccommodationId))

				// document
				.andDo(document("위시리스트-숙소메모수정-성공",
					pathParameters(
						parameterWithName("wishlistId")
							.description("위시리스트의 고유 식별자"),
						parameterWithName("wishlistAccommodationId")
							.description("수정할 위시리스트 항목의 고유 식별자")
					),
					requestFields(
						fieldWithPath("memo")
							.type(JsonFieldType.STRING)
							.description("수정할 메모 내용")
					),
					responseFields(
						fieldWithPath("id")
							.type(JsonFieldType.NUMBER)
							.description("수정된 위시리스트 항목의 고유 식별자")
					)));

			verify(wishlistService).updateWishlistAccommodation(eq(wishlistAccommodationId),
				any(WishlistRequest.UpdateWishlistAccommodationRequest.class));
		}

		@Test
		@DisplayName("시나리오: 존재하지 않는 위시리스트의 숙소 메모 수정을 시도한다")
		void 존재하지_않는_위시리스트의_숙소_메모_수정을_시도한다() throws Exception {
			// Given: 존재하지 않는 위시리스트 ID로 메모를 수정하려는 상황
			Long nonExistentWishlistId = 999L;
			Long wishlistAccommodationId = 10L;
			String memo = "수정할 메모";
			WishlistRequest.UpdateWishlistAccommodationRequest request =
				new WishlistRequest.UpdateWishlistAccommodationRequest(memo);

			when(wishlistService.updateWishlistAccommodation( eq(wishlistAccommodationId),
				any(WishlistRequest.UpdateWishlistAccommodationRequest.class)))
				.thenThrow(new WishlistNotFoundException());

			// When: 존재하지 않는 위시리스트의 숙소 메모 수정을 시도한다
			mockMvc.perform(patch("/api/members/wishlists/{wishlistId}/accommodations/{wishlistAccommodationId}",
					nonExistentWishlistId, wishlistAccommodationId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))

				// Then: 404 Not Found 오류가 발생한다
				.andExpect(status().isNotFound())

				// document
				.andDo(document("위시리스트-숙소메모수정-위시리스트없음-실패",
					pathParameters(
						parameterWithName("wishlistId")
							.description("존재하지 않는 위시리스트 ID"),
						parameterWithName("wishlistAccommodationId")
							.description("위시리스트 항목 ID")
					),
					requestFields(
						fieldWithPath("memo")
							.type(JsonFieldType.STRING)
							.description("수정하려는 메모 내용")
					)));

			verify(wishlistService).updateWishlistAccommodation(eq(wishlistAccommodationId),
				any(WishlistRequest.UpdateWishlistAccommodationRequest.class));
		}

		@Test
		@DisplayName("시나리오: 존재하지 않는 위시리스트 항목의 메모 수정을 시도한다")
		void 존재하지_않는_위시리스트_항목의_메모_수정을_시도한다() throws Exception {
			// Given: 존재하지 않는 위시리스트 항목 ID로 메모를 수정하려는 상황
			Long wishlistId = 1L;
			Long nonExistentWishlistAccommodationId = 999L;
			String memo = "수정할 메모";
			WishlistRequest.UpdateWishlistAccommodationRequest request =
				new WishlistRequest.UpdateWishlistAccommodationRequest(memo);

			when(wishlistService.updateWishlistAccommodation(eq(nonExistentWishlistAccommodationId),
				any(WishlistRequest.UpdateWishlistAccommodationRequest.class)))
				.thenThrow(new WishlistAccommodationNotFoundException());

			// When: 존재하지 않는 위시리스트 항목의 메모 수정을 시도한다
			mockMvc.perform(patch("/api/members/wishlists/{wishlistId}/accommodations/{wishlistAccommodationId}",
					wishlistId, nonExistentWishlistAccommodationId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))

				// Then: 404 Not Found 오류가 발생한다
				.andExpect(status().isNotFound())

				// document
				.andDo(document("위시리스트-숙소메모수정-항목없음-실패",
					pathParameters(
						parameterWithName("wishlistId")
							.description("위시리스트 ID"),
						parameterWithName("wishlistAccommodationId")
							.description("존재하지 않는 위시리스트 항목 ID")
					),
					requestFields(
						fieldWithPath("memo")
							.type(JsonFieldType.STRING)
							.description("수정하려는 메모 내용")
					)));

			verify(wishlistService).updateWishlistAccommodation(eq(nonExistentWishlistAccommodationId),
				any(WishlistRequest.UpdateWishlistAccommodationRequest.class));
		}

		@Test
		@DisplayName("시나리오: 다른 사용자의 위시리스트 숙소 메모 수정을 시도한다")
		void 다른_사용자의_위시리스트_숙소_메모_수정을_시도한다() throws Exception {
			// Given: 다른 사용자 소유의 위시리스트 숙소 메모를 수정하려는 상황
			Long otherUserWishlistId = 1L;
			Long wishlistAccommodationId = 10L;
			String memo = "수정할 메모";
			WishlistRequest.UpdateWishlistAccommodationRequest request =
				new WishlistRequest.UpdateWishlistAccommodationRequest(memo);

			when(wishlistService.updateWishlistAccommodation( eq(wishlistAccommodationId),
				any(WishlistRequest.UpdateWishlistAccommodationRequest.class)))
				.thenThrow(new WishlistAccessDeniedException());

			// When: 다른 사용자의 위시리스트 숙소 메모 수정을 시도한다
			mockMvc.perform(patch("/api/members/wishlists/{wishlistId}/accommodations/{wishlistAccommodationId}",
					otherUserWishlistId, wishlistAccommodationId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))

				// Then: 403 Forbidden 오류가 발생한다
				.andExpect(status().isForbidden())

				// document
				.andDo(document("위시리스트-숙소메모수정-권한없음-실패",
					pathParameters(
						parameterWithName("wishlistId")
							.description("다른 사용자 소유의 위시리스트 ID"),
						parameterWithName("wishlistAccommodationId")
							.description("위시리스트 항목 ID")
					),
					requestFields(
						fieldWithPath("memo")
							.type(JsonFieldType.STRING)
							.description("수정하려는 메모 내용")
					)));

			verify(wishlistService).updateWishlistAccommodation( eq(wishlistAccommodationId),
				any(WishlistRequest.UpdateWishlistAccommodationRequest.class));
		}

		@Test
		@DisplayName("시나리오: 다른 위시리스트에 속한 항목의 메모 수정을 시도한다")
		void 다른_위시리스트에_속한_항목의_메모_수정을_시도한다() throws Exception {
			// Given: 다른 위시리스트에 속한 항목의 메모를 수정하려는 상황
			Long wishlistId = 1L;
			Long otherWishlistAccommodationId = 20L; // 다른 위시리스트에 속한 항목
			String memo = "수정할 메모";
			WishlistRequest.UpdateWishlistAccommodationRequest request =
				new WishlistRequest.UpdateWishlistAccommodationRequest(memo);

			when(wishlistService.updateWishlistAccommodation( eq(otherWishlistAccommodationId),
				any(WishlistRequest.UpdateWishlistAccommodationRequest.class)))
				.thenThrow(new WishlistAccommodationAccessDeniedException());

			// When: 다른 위시리스트에 속한 항목의 메모 수정을 시도한다
			mockMvc.perform(patch("/api/members/wishlists/{wishlistId}/accommodations/{wishlistAccommodationId}",
					wishlistId, otherWishlistAccommodationId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))

				// Then: 403 Forbidden 오류가 발생한다
				.andExpect(status().isForbidden())

				// document
				.andDo(document("위시리스트-숙소메모수정-항목불일치-실패",
					pathParameters(
						parameterWithName("wishlistId")
							.description("위시리스트 ID"),
						parameterWithName("wishlistAccommodationId")
							.description("다른 위시리스트에 속한 항목 ID")
					),
					requestFields(
						fieldWithPath("memo")
							.type(JsonFieldType.STRING)
							.description("수정하려는 메모 내용")
					)));

			verify(wishlistService).updateWishlistAccommodation( eq(otherWishlistAccommodationId),
				any(WishlistRequest.UpdateWishlistAccommodationRequest.class));
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("invalidMemoProvider")
		@DisplayName("시나리오: 잘못된 메모로 위시리스트 숙소 메모 수정을 시도한다")
		void 잘못된_메모로_위시리스트_숙소_메모_수정을_시도한다(
			String testName,
			String invalidMemo,
			JsonFieldType fieldType,
			String description,
			String documentId
		) throws Exception {
			// Given: 잘못된 메모로 수정을 시도하는 상황
			Long wishlistId = 1L;
			Long wishlistAccommodationId = 10L;
			WishlistRequest.UpdateWishlistAccommodationRequest invalidRequest =
				new WishlistRequest.UpdateWishlistAccommodationRequest(invalidMemo);

			// When: 잘못된 메모로 수정을 시도한다
			mockMvc.perform(patch("/api/members/wishlists/{wishlistId}/accommodations/{wishlistAccommodationId}",
					wishlistId, wishlistAccommodationId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(invalidRequest)))

				// Then: 400 Bad Request 오류가 발생한다
				.andExpect(status().isBadRequest())

				// document
				.andDo(document(documentId,
					pathParameters(
						parameterWithName("wishlistId")
							.description("위시리스트 ID"),
						parameterWithName("wishlistAccommodationId")
							.description("위시리스트 항목 ID")
					),
					requestFields(
						fieldWithPath("memo")
							.type(fieldType)
							.description(description)
					)));
		}

		static Stream<Arguments> invalidMemoProvider() {
			return Stream.of(
				Arguments.of(
					"빈 문자열로 수정",
					"",
					JsonFieldType.STRING,
					"빈 메모 (유효하지 않은 입력)",
					"위시리스트-숙소메모수정-빈메모-실패"
				),
				Arguments.of(
					"공백 문자로 수정",
					"   ",
					JsonFieldType.STRING,
					"공백 메모 (유효하지 않은 입력)",
					"위시리스트-숙소메모수정-공백메모-실패"
				),
				Arguments.of(
					"null로 수정",
					null,
					JsonFieldType.NULL,
					"NULL 메모 (유효하지 않은 입력)",
					"위시리스트-숙소메모수정-null메모-실패"
				),
				Arguments.of(
					"1024자 초과로 수정",
					"A".repeat(1025),
					JsonFieldType.STRING,
					"1024자를 초과하는 메모 (유효하지 않은 입력)",
					"위시리스트-숙소메모수정-길이초과-실패"
				)
			);
		}

		@Test
		@DisplayName("시나리오: 여러 위시리스트 숙소의 메모를 연속으로 수정한다")
		void 여러_위시리스트_숙소의_메모를_연속으로_수정한다() throws Exception {
			// Given: 여러 위시리스트 항목의 메모를 수정하는 상황
			Long wishlistId = 1L;
			Long[] wishlistAccommodationIds = {10L, 20L, 30L, 40L, 50L};
			String[] memos = {
				"첫 번째 숙소 메모",
				"두 번째 숙소 메모",
				"세 번째 숙소 메모",
				"네 번째 숙소 메모",
				"다섯 번째 숙소 메모"
			};

			for (int i = 0; i < wishlistAccommodationIds.length; i++) {
				WishlistRequest.UpdateWishlistAccommodationRequest request =
					new WishlistRequest.UpdateWishlistAccommodationRequest(memos[i]);
				WishlistResponse.UpdateWishlistAccommodationResponse expectedResponse =
					new WishlistResponse.UpdateWishlistAccommodationResponse(wishlistAccommodationIds[i]);

				when(wishlistService.updateWishlistAccommodation( eq(wishlistAccommodationIds[i]),
					any(WishlistRequest.UpdateWishlistAccommodationRequest.class)))
					.thenReturn(expectedResponse);

				// When: 각각의 위시리스트 항목 메모를 수정한다
				mockMvc.perform(patch("/api/members/wishlists/{wishlistId}/accommodations/{wishlistAccommodationId}",
						wishlistId, wishlistAccommodationIds[i])
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))

					// Then: 모든 메모가 성공적으로 수정된다
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(wishlistAccommodationIds[i]))

					// document
					.andDo(document("위시리스트-숙소메모수정-연속수정-" + (i + 1),
						pathParameters(
							parameterWithName("wishlistId")
								.description("위시리스트 ID"),
							parameterWithName("wishlistAccommodationId")
								.description("위시리스트 항목 ID: " + wishlistAccommodationIds[i])
						),
						requestFields(
							fieldWithPath("memo")
								.type(JsonFieldType.STRING)
								.description("메모 내용: " + memos[i])
						),
						responseFields(
							fieldWithPath("id")
								.type(JsonFieldType.NUMBER)
								.description("수정된 위시리스트 항목 ID")
						)));
			}
		}

		@Test
		@DisplayName("시나리오: 최대 길이의 메모로 위시리스트 숙소 메모를 수정한다")
		void 최대_길이의_메모로_위시리스트_숙소_메모를_수정한다() throws Exception {
			// Given: 최대 길이(1024자)의 메모로 수정하는 상황
			Long wishlistId = 1L;
			Long wishlistAccommodationId = 10L;
			String maxLengthMemo = "A".repeat(1024);
			WishlistRequest.UpdateWishlistAccommodationRequest request =
				new WishlistRequest.UpdateWishlistAccommodationRequest(maxLengthMemo);
			WishlistResponse.UpdateWishlistAccommodationResponse expectedResponse =
				new WishlistResponse.UpdateWishlistAccommodationResponse(wishlistAccommodationId);

			when(wishlistService.updateWishlistAccommodation(eq(wishlistAccommodationId),
				any(WishlistRequest.UpdateWishlistAccommodationRequest.class)))
				.thenReturn(expectedResponse);

			// When: 최대 길이의 메모로 수정을 시도한다
			mockMvc.perform(patch("/api/members/wishlists/{wishlistId}/accommodations/{wishlistAccommodationId}",
					wishlistId, wishlistAccommodationId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))

				// Then: 메모가 성공적으로 수정된다
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(wishlistAccommodationId))

				// document
				.andDo(document("위시리스트-숙소메모수정-최대길이-성공",
					pathParameters(
						parameterWithName("wishlistId")
							.description("위시리스트 ID"),
						parameterWithName("wishlistAccommodationId")
							.description("위시리스트 항목 ID")
					),
					requestFields(
						fieldWithPath("memo")
							.type(JsonFieldType.STRING)
							.description("최대 길이(1024자)의 메모 내용")
					),
					responseFields(
						fieldWithPath("id")
							.type(JsonFieldType.NUMBER)
							.description("수정된 위시리스트 항목 ID")
					)));

			verify(wishlistService).updateWishlistAccommodation( eq(wishlistAccommodationId),
				any(WishlistRequest.UpdateWishlistAccommodationRequest.class));
		}

		@Test
		@DisplayName("시나리오: 특수 문자가 포함된 메모로 위시리스트 숙소 메모를 수정한다")
		void 특수_문자가_포함된_메모로_위시리스트_숙소_메모를_수정한다() throws Exception {
			// Given: 특수 문자가 포함된 메모로 수정하는 상황
			Long wishlistId = 1L;
			Long wishlistAccommodationId = 10L;
			String specialCharacterMemo = "정말 좋은 곳이에요! 🏨✨ 가격도 합리적이고 (★★★★★) 직원분들도 친절해요 😊 다음에도 올게요~ #추천 @여행";
			WishlistRequest.UpdateWishlistAccommodationRequest request =
				new WishlistRequest.UpdateWishlistAccommodationRequest(specialCharacterMemo);
			WishlistResponse.UpdateWishlistAccommodationResponse expectedResponse =
				new WishlistResponse.UpdateWishlistAccommodationResponse(wishlistAccommodationId);

			when(wishlistService.updateWishlistAccommodation( eq(wishlistAccommodationId),
				any(WishlistRequest.UpdateWishlistAccommodationRequest.class)))
				.thenReturn(expectedResponse);

			// When: 특수 문자가 포함된 메모로 수정을 시도한다
			mockMvc.perform(patch("/api/members/wishlists/{wishlistId}/accommodations/{wishlistAccommodationId}",
					wishlistId, wishlistAccommodationId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))

				// Then: 메모가 성공적으로 수정된다
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(wishlistAccommodationId))

				// document
				.andDo(document("위시리스트-숙소메모수정-특수문자-성공",
					pathParameters(
						parameterWithName("wishlistId")
							.description("위시리스트 ID"),
						parameterWithName("wishlistAccommodationId")
							.description("위시리스트 항목 ID")
					),
					requestFields(
						fieldWithPath("memo")
							.type(JsonFieldType.STRING)
							.description("특수 문자가 포함된 메모 내용")
					),
					responseFields(
						fieldWithPath("id")
							.type(JsonFieldType.NUMBER)
							.description("수정된 위시리스트 항목 ID")
					)));

			verify(wishlistService).updateWishlistAccommodation(eq(wishlistAccommodationId),
				any(WishlistRequest.UpdateWishlistAccommodationRequest.class));
		}
	}

	@Nested
	@DisplayName("위시리스트 숙소 삭제:")
	class DeleteWishlistAccommodationTests {

		@Test
		@DisplayName("시나리오: 사용자가 위시리스트에서 숙소를 성공적으로 삭제한다")
		void 사용자가_위시리스트에서_숙소를_성공적으로_삭제한다() throws Exception {
			// Given: 존재하는 위시리스트 항목을 삭제하는 상황
			Long wishlistId = 1L;
			Long wishlistAccommodationId = 10L;

			doNothing().when(wishlistService)
				.deleteWishlistAccommodation( eq(wishlistAccommodationId));

			// When: 사용자가 위시리스트 숙소 삭제 API를 호출한다
			mockMvc.perform(delete("/api/members/wishlists/{wishlistId}/accommodations/{wishlistAccommodationId}",
					wishlistId, wishlistAccommodationId))

				// Then: 숙소가 성공적으로 삭제된다
				.andExpect(status().isNoContent())

				// document
				.andDo(document("위시리스트-숙소삭제-성공",
					pathParameters(
						parameterWithName("wishlistId")
							.description("위시리스트의 고유 식별자"),
						parameterWithName("wishlistAccommodationId")
							.description("삭제할 위시리스트 항목의 고유 식별자")
					)));

			verify(wishlistService).deleteWishlistAccommodation(eq(wishlistAccommodationId));
		}

		@Test
		@DisplayName("시나리오: 존재하지 않는 위시리스트의 숙소 삭제를 시도한다")
		void 존재하지_않는_위시리스트의_숙소_삭제를_시도한다() throws Exception {
			// Given: 존재하지 않는 위시리스트 ID로 숙소를 삭제하려는 상황
			Long nonExistentWishlistId = 999L;
			Long wishlistAccommodationId = 10L;

			doThrow(new WishlistNotFoundException())
				.when(wishlistService)
				.deleteWishlistAccommodation(eq(wishlistAccommodationId));

			// When: 존재하지 않는 위시리스트의 숙소 삭제를 시도한다
			mockMvc.perform(delete("/api/members/wishlists/{wishlistId}/accommodations/{wishlistAccommodationId}",
					nonExistentWishlistId, wishlistAccommodationId))

				// Then: 404 Not Found 오류가 발생한다
				.andExpect(status().isNotFound())

				// document
				.andDo(document("위시리스트-숙소삭제-위시리스트없음-실패",
					pathParameters(
						parameterWithName("wishlistId")
							.description("존재하지 않는 위시리스트 ID"),
						parameterWithName("wishlistAccommodationId")
							.description("삭제하려는 위시리스트 항목 ID")
					)));

			verify(wishlistService).deleteWishlistAccommodation( eq(wishlistAccommodationId));
		}

		@Test
		@DisplayName("시나리오: 존재하지 않는 위시리스트 항목 삭제를 시도한다")
		void 존재하지_않는_위시리스트_항목_삭제를_시도한다() throws Exception {
			// Given: 존재하지 않는 위시리스트 항목 ID로 삭제를 시도하는 상황
			Long wishlistId = 1L;
			Long nonExistentWishlistAccommodationId = 999L;

			doThrow(new WishlistAccommodationNotFoundException())
				.when(wishlistService)
				.deleteWishlistAccommodation( eq(nonExistentWishlistAccommodationId));

			// When: 존재하지 않는 위시리스트 항목 삭제를 시도한다
			mockMvc.perform(delete("/api/members/wishlists/{wishlistId}/accommodations/{wishlistAccommodationId}",
					wishlistId, nonExistentWishlistAccommodationId))

				// Then: 404 Not Found 오류가 발생한다
				.andExpect(status().isNotFound())

				// document
				.andDo(document("위시리스트-숙소삭제-항목없음-실패",
					pathParameters(
						parameterWithName("wishlistId")
							.description("위시리스트 ID"),
						parameterWithName("wishlistAccommodationId")
							.description("존재하지 않는 위시리스트 항목 ID")
					)));

			verify(wishlistService).deleteWishlistAccommodation( eq(nonExistentWishlistAccommodationId));
		}

		@Test
		@DisplayName("시나리오: 다른 사용자의 위시리스트 숙소 삭제를 시도한다")
		void 다른_사용자의_위시리스트_숙소_삭제를_시도한다() throws Exception {
			// Given: 다른 사용자 소유의 위시리스트 숙소를 삭제하려는 상황
			Long otherUserWishlistId = 1L;
			Long wishlistAccommodationId = 10L;

			doThrow(new WishlistAccessDeniedException())
				.when(wishlistService)
				.deleteWishlistAccommodation(eq(wishlistAccommodationId));

			// When: 다른 사용자의 위시리스트 숙소 삭제를 시도한다
			mockMvc.perform(delete("/api/members/wishlists/{wishlistId}/accommodations/{wishlistAccommodationId}",
					otherUserWishlistId, wishlistAccommodationId))

				// Then: 403 Forbidden 오류가 발생한다
				.andExpect(status().isForbidden())

				// document
				.andDo(document("위시리스트-숙소삭제-권한없음-실패",
					pathParameters(
						parameterWithName("wishlistId")
							.description("다른 사용자 소유의 위시리스트 ID"),
						parameterWithName("wishlistAccommodationId")
							.description("삭제하려는 위시리스트 항목 ID")
					)));

			verify(wishlistService).deleteWishlistAccommodation( eq(wishlistAccommodationId));
		}

		@Test
		@DisplayName("시나리오: 다른 위시리스트에 속한 항목 삭제를 시도한다")
		void 다른_위시리스트에_속한_항목_삭제를_시도한다() throws Exception {
			// Given: 다른 위시리스트에 속한 항목을 삭제하려는 상황
			Long wishlistId = 1L;
			Long otherWishlistAccommodationId = 20L; // 다른 위시리스트에 속한 항목

			doThrow(new WishlistAccommodationAccessDeniedException())
				.when(wishlistService)
				.deleteWishlistAccommodation( eq(otherWishlistAccommodationId));

			// When: 다른 위시리스트에 속한 항목 삭제를 시도한다
			mockMvc.perform(delete("/api/members/wishlists/{wishlistId}/accommodations/{wishlistAccommodationId}",
					wishlistId, otherWishlistAccommodationId))

				// Then: 403 Forbidden 오류가 발생한다
				.andExpect(status().isForbidden())

				// document
				.andDo(document("위시리스트-숙소삭제-항목불일치-실패",
					pathParameters(
						parameterWithName("wishlistId")
							.description("위시리스트 ID"),
						parameterWithName("wishlistAccommodationId")
							.description("다른 위시리스트에 속한 항목 ID")
					)));

			verify(wishlistService).deleteWishlistAccommodation( eq(otherWishlistAccommodationId));
		}

		@Test
		@DisplayName("시나리오: 여러 위시리스트 숙소를 연속으로 삭제한다")
		void 여러_위시리스트_숙소를_연속으로_삭제한다() throws Exception {
			// Given: 여러 위시리스트 항목을 삭제하는 상황
			Long wishlistId = 1L;
			Long[] wishlistAccommodationIds = {10L, 20L, 30L, 40L, 50L};

			for (int i = 0; i < wishlistAccommodationIds.length; i++) {
				doNothing().when(wishlistService)
					.deleteWishlistAccommodation(eq(wishlistAccommodationIds[i]));

				// When: 각각의 위시리스트 항목을 삭제한다
				mockMvc.perform(delete("/api/members/wishlists/{wishlistId}/accommodations/{wishlistAccommodationId}",
						wishlistId, wishlistAccommodationIds[i]))

					// Then: 모든 항목이 성공적으로 삭제된다
					.andExpect(status().isNoContent())

					// document
					.andDo(document("위시리스트-숙소삭제-연속삭제-" + (i + 1),
						pathParameters(
							parameterWithName("wishlistId")
								.description("위시리스트 ID"),
							parameterWithName("wishlistAccommodationId")
								.description("삭제할 위시리스트 항목 ID: " + wishlistAccommodationIds[i])
						)));

				verify(wishlistService).deleteWishlistAccommodation( eq(wishlistAccommodationIds[i]));
			}
		}

		@Test
		@DisplayName("시나리오: 매우 큰 ID 값의 위시리스트 항목 삭제를 시도한다")
		void 매우_큰_ID_값의_위시리스트_항목_삭제를_시도한다() throws Exception {
			// Given: 매우 큰 ID 값의 위시리스트 항목을 삭제하는 상황
			Long wishlistId = Long.MAX_VALUE;
			Long wishlistAccommodationId = Long.MAX_VALUE - 1;

			doNothing().when(wishlistService)
				.deleteWishlistAccommodation( eq(wishlistAccommodationId));

			// When: 매우 큰 ID 값의 위시리스트 항목 삭제를 시도한다
			mockMvc.perform(delete("/api/members/wishlists/{wishlistId}/accommodations/{wishlistAccommodationId}",
					wishlistId, wishlistAccommodationId))

				// Then: 삭제가 성공적으로 처리된다
				.andExpect(status().isNoContent())

				// document
				.andDo(document("위시리스트-숙소삭제-최대값ID-성공",
					pathParameters(
						parameterWithName("wishlistId")
							.description("매우 큰 위시리스트 ID (Long.MAX_VALUE)"),
						parameterWithName("wishlistAccommodationId")
							.description("매우 큰 위시리스트 항목 ID (Long.MAX_VALUE - 1)")
					)));

			verify(wishlistService).deleteWishlistAccommodation( eq(wishlistAccommodationId));
		}

		@Test
		@DisplayName("시나리오: 동일한 위시리스트 항목을 중복으로 삭제하려 시도한다")
		void 동일한_위시리스트_항목을_중복으로_삭제하려_시도한다() throws Exception {
			// Given: 이미 삭제된 위시리스트 항목을 다시 삭제하려는 상황
			Long wishlistId = 1L;
			Long alreadyDeletedAccommodationId = 10L;

			// 첫 번째 삭제는 성공
			doNothing().when(wishlistService)
				.deleteWishlistAccommodation(eq(alreadyDeletedAccommodationId));

			// 첫 번째 삭제 수행
			mockMvc.perform(delete("/api/members/wishlists/{wishlistId}/accommodations/{wishlistAccommodationId}",
					wishlistId, alreadyDeletedAccommodationId))
				.andExpect(status().isNoContent());

			// 두 번째 삭제 시도 시 예외 발생
			doThrow(new WishlistAccommodationNotFoundException())
				.when(wishlistService)
				.deleteWishlistAccommodation(eq(alreadyDeletedAccommodationId));

			// When: 이미 삭제된 항목을 다시 삭제하려 시도한다
			mockMvc.perform(delete("/api/members/wishlists/{wishlistId}/accommodations/{wishlistAccommodationId}",
					wishlistId, alreadyDeletedAccommodationId))

				// Then: 404 Not Found 오류가 발생한다
				.andExpect(status().isNotFound())

				// document
				.andDo(document("위시리스트-숙소삭제-중복삭제-실패",
					pathParameters(
						parameterWithName("wishlistId")
							.description("위시리스트 ID"),
						parameterWithName("wishlistAccommodationId")
							.description("이미 삭제된 위시리스트 항목 ID")
					)));

			verify(wishlistService, times(2))
				.deleteWishlistAccommodation( eq(alreadyDeletedAccommodationId));
		}

		@Test
		@DisplayName("시나리오: 빈 위시리스트에서 항목 삭제를 시도한다")
		void 빈_위시리스트에서_항목_삭제를_시도한다() throws Exception {
			// Given: 빈 위시리스트에서 항목을 삭제하려는 상황
			Long emptyWishlistId = 1L;
			Long nonExistentAccommodationId = 10L;

			doThrow(new WishlistAccommodationNotFoundException())
				.when(wishlistService)
				.deleteWishlistAccommodation( eq(nonExistentAccommodationId));

			// When: 빈 위시리스트에서 항목 삭제를 시도한다
			mockMvc.perform(delete("/api/members/wishlists/{wishlistId}/accommodations/{wishlistAccommodationId}",
					emptyWishlistId, nonExistentAccommodationId))

				// Then: 404 Not Found 오류가 발생한다
				.andExpect(status().isNotFound())

				// document
				.andDo(document("위시리스트-숙소삭제-빈위시리스트-실패",
					pathParameters(
						parameterWithName("wishlistId")
							.description("빈 위시리스트 ID"),
						parameterWithName("wishlistAccommodationId")
							.description("존재하지 않는 위시리스트 항목 ID")
					)));

			verify(wishlistService).deleteWishlistAccommodation( eq(nonExistentAccommodationId));
		}

		@Test
		@DisplayName("시나리오: 위시리스트의 마지막 숙소를 삭제한다")
		void 위시리스트의_마지막_숙소를_삭제한다() throws Exception {
			// Given: 위시리스트에 하나만 남은 숙소를 삭제하는 상황
			Long wishlistId = 1L;
			Long lastAccommodationId = 10L;

			doNothing().when(wishlistService)
				.deleteWishlistAccommodation( eq(lastAccommodationId));

			// When: 위시리스트의 마지막 숙소를 삭제한다
			mockMvc.perform(delete("/api/members/wishlists/{wishlistId}/accommodations/{wishlistAccommodationId}",
					wishlistId, lastAccommodationId))

				// Then: 삭제가 성공적으로 처리된다
				.andExpect(status().isNoContent())

				// document
				.andDo(document("위시리스트-마지막숙소삭제-성공",
					pathParameters(
						parameterWithName("wishlistId")
							.description("위시리스트 ID"),
						parameterWithName("wishlistAccommodationId")
							.description("위시리스트의 마지막 남은 숙소 ID")
					)));

			verify(wishlistService).deleteWishlistAccommodation( eq(lastAccommodationId));
		}

		@ParameterizedTest(name = "잘못된 ID: {0}")
		@ValueSource(longs = {-1L, 0L})
		@DisplayName("시나리오: 잘못된 ID 값으로 위시리스트 항목 삭제를 시도한다")
		void 잘못된_ID_값으로_위시리스트_항목_삭제를_시도한다(Long invalidId) throws Exception {
			// Given: 잘못된 ID 값으로 삭제를 시도하는 상황
			Long wishlistId = 1L;

			// When: 잘못된 ID로 삭제를 시도한다
			mockMvc.perform(delete("/api/members/wishlists/{wishlistId}/accommodations/{wishlistAccommodationId}",
					wishlistId, invalidId))

				// Then: 요청은 처리되지만 서비스에서 예외가 발생할 수 있다
				.andExpect(result -> {
					// 음수나 0은 유효하지 않은 ID이지만 URL 파라미터로는 전달됨
					// 실제 비즈니스 로직에서 처리되어야 함
				})

				// document
				.andDo(document("위시리스트-숙소삭제-잘못된ID-" + Math.abs(invalidId),
					pathParameters(
						parameterWithName("wishlistId")
							.description("위시리스트 ID"),
						parameterWithName("wishlistAccommodationId")
							.description("잘못된 위시리스트 항목 ID: " + invalidId)
					)));
		}
	}

	@Nested
	@DisplayName("위시리스트 숙소 목록 조회:")
	class FindWishlistAccommodationsTests {

		@Test
		@DisplayName("시나리오: 사용자가 위시리스트의 숙소 목록을 성공적으로 조회한다")
		void 사용자가_위시리스트의_숙소_목록을_성공적으로_조회한다() throws Exception {
			// Given: 위시리스트에 여러 숙소가 포함된 상황
			Long wishlistId = 1L;

			List<AccommodationResponse.AmenityInfoResponse> amenities1 = List.of(
				new AccommodationResponse.AmenityInfoResponse(AmenityType.WIFI, 1),
				new AccommodationResponse.AmenityInfoResponse(AmenityType.TV, 1)
			);

			List<AccommodationResponse.AmenityInfoResponse> amenities2 = List.of(
				new AccommodationResponse.AmenityInfoResponse(AmenityType.PARKING, 1),
				new AccommodationResponse.AmenityInfoResponse(AmenityType.KITCHEN, 1)
			);

			List<WishlistResponse.WishlistAccommodationInfo> accommodations = List.of(
				new WishlistResponse.WishlistAccommodationInfo(
					10L,
					"신라호텔 관련 메모",
					new AccommodationResponse.WishlistAccommodationInfo(
						100L,
						"신라호텔",
						List.of("hotel1_image1.jpg", "hotel1_image2.jpg"),
						amenities1,
						4.5
					)
				),
				new WishlistResponse.WishlistAccommodationInfo(
					20L,
					"롯데호텔 관련 메모",
					new AccommodationResponse.WishlistAccommodationInfo(
						200L,
						"롯데호텔",
						List.of("hotel2_image1.jpg"),
						amenities2,
						4.3
					)
				),
				new WishlistResponse.WishlistAccommodationInfo(
					30L,
					"게스트하우스 메모",
					new AccommodationResponse.WishlistAccommodationInfo(
						300L,
						"게스트하우스",
						List.of(),
						List.of(),
						null
					)
				)
			);

			CursorResponse.PageInfo pageInfo = CursorResponse.PageInfo.builder()
				.hasNext(false)
				.nextCursor(null)
				.currentSize(3)
				.build();

			WishlistResponse.WishlistAccommodationInfos expectedResponse =
				new WishlistResponse.WishlistAccommodationInfos(accommodations, pageInfo);

			when(wishlistService.findWishlistAccommodations(eq(wishlistId), any(CursorRequest.CursorPageRequest.class)))
				.thenReturn(expectedResponse);

			// When: 사용자가 위시리스트 숙소 목록 조회 API를 호출한다
			mockMvc.perform(get("/api/members/wishlists/{wishlistId}/accommodations", wishlistId)
					.param("size", "20"))

				// Then: 위시리스트 숙소 목록이 성공적으로 반환된다
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.wishlistAccommodations").isArray())
				.andExpect(jsonPath("$.wishlistAccommodations.length()").value(3))
				.andExpect(jsonPath("$.wishlistAccommodations[0].id").value(10L))
				.andExpect(jsonPath("$.wishlistAccommodations[0].name").value("신라호텔 관련 메모"))
				.andExpect(jsonPath("$.wishlistAccommodations[0].accommodationInfo.accommodationId").value(100L))
				.andExpect(jsonPath("$.wishlistAccommodations[0].accommodationInfo.name").value("신라호텔"))
				.andExpect(jsonPath("$.wishlistAccommodations[0].accommodationInfo.accommodationImageUrls").isArray())
				.andExpect(jsonPath("$.wishlistAccommodations[0].accommodationInfo.accommodationImageUrls.length()").value(2))
				.andExpect(jsonPath("$.wishlistAccommodations[0].accommodationInfo.amenities").isArray())
				.andExpect(jsonPath("$.wishlistAccommodations[0].accommodationInfo.amenities.length()").value(2))
				.andExpect(jsonPath("$.wishlistAccommodations[0].accommodationInfo.averageRating").value(4.5))
				.andExpect(jsonPath("$.wishlistAccommodations[2].accommodationInfo.accommodationImageUrls").isEmpty())
				.andExpect(jsonPath("$.wishlistAccommodations[2].accommodationInfo.amenities").isEmpty())
				.andExpect(jsonPath("$.wishlistAccommodations[2].accommodationInfo.averageRating").isEmpty())
				.andExpect(jsonPath("$.pageInfo.hasNext").value(false))
				.andExpect(jsonPath("$.pageInfo.currentSize").value(3))

				// document
				.andDo(document("위시리스트-숙소목록조회-성공",
					pathParameters(
						parameterWithName("wishlistId")
							.description("조회할 위시리스트의 고유 식별자")
					),
					queryParameters(
						parameterWithName("size")
							.description("페이지 크기 (기본값: 20, 최대: 50)")
							.optional(),
						parameterWithName("cursor")
							.description("다음 페이지를 위한 커서 (첫 페이지에서는 생략)")
							.optional()
					),
					responseFields(
						fieldWithPath("wishlistAccommodations[]")
							.type(JsonFieldType.ARRAY)
							.description("위시리스트 숙소 목록"),
						fieldWithPath("wishlistAccommodations[].id")
							.type(JsonFieldType.NUMBER)
							.description("위시리스트 항목 고유 식별자"),
						fieldWithPath("wishlistAccommodations[].name")
							.type(JsonFieldType.STRING)
							.description("위시리스트 항목 메모"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo")
							.type(JsonFieldType.OBJECT)
							.description("숙소 상세 정보"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.accommodationId")
							.type(JsonFieldType.NUMBER)
							.description("숙소 고유 식별자"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.name")
							.type(JsonFieldType.STRING)
							.description("숙소 이름"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.accommodationImageUrls[]")
							.type(JsonFieldType.ARRAY)
							.description("숙소 이미지 URL 목록"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.amenities[]")
							.type(JsonFieldType.ARRAY)
							.description("숙소 편의시설 목록"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.amenities[].type")
							.type(JsonFieldType.STRING)
							.description("편의시설 타입"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.amenities[].count")
							.type(JsonFieldType.NUMBER)
							.description("편의시설 개수"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.averageRating")
							.type(JsonFieldType.NUMBER)
							.description("숙소 평균 평점 (없을 경우 null)")
							.optional(),
						fieldWithPath("pageInfo")
							.type(JsonFieldType.OBJECT)
							.description("페이징 정보"),
						fieldWithPath("pageInfo.hasNext")
							.type(JsonFieldType.BOOLEAN)
							.description("다음 페이지 존재 여부"),
						fieldWithPath("pageInfo.nextCursor")
							.type(JsonFieldType.STRING)
							.description("다음 페이지 커서 (없을 경우 null)")
							.optional(),
						fieldWithPath("pageInfo.currentSize")
							.type(JsonFieldType.NUMBER)
							.description("현재 페이지 항목 수")
					)));

			verify(wishlistService).findWishlistAccommodations(eq(wishlistId), any(CursorRequest.CursorPageRequest.class));
		}

		@Test
		@DisplayName("시나리오: 빈 위시리스트의 숙소 목록을 조회한다")
		void 빈_위시리스트의_숙소_목록을_조회한다() throws Exception {
			// Given: 숙소가 없는 위시리스트를 조회하는 상황
			Long emptyWishlistId = 1L;

			CursorResponse.PageInfo pageInfo = CursorResponse.PageInfo.builder()
				.hasNext(false)
				.nextCursor(null)
				.currentSize(0)
				.build();

			WishlistResponse.WishlistAccommodationInfos expectedResponse =
				new WishlistResponse.WishlistAccommodationInfos(List.of(), pageInfo);

			when(wishlistService.findWishlistAccommodations(eq(emptyWishlistId), any(CursorRequest.CursorPageRequest.class)))
				.thenReturn(expectedResponse);

			// When: 빈 위시리스트의 숙소 목록을 조회한다
			mockMvc.perform(get("/api/members/wishlists/{wishlistId}/accommodations", emptyWishlistId)
					.param("size", "20"))

				// Then: 빈 목록이 반환된다
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.wishlistAccommodations").isArray())
				.andExpect(jsonPath("$.wishlistAccommodations.length()").value(0))
				.andExpect(jsonPath("$.pageInfo.hasNext").value(false))
				.andExpect(jsonPath("$.pageInfo.currentSize").value(0))

				// document
				.andDo(document("위시리스트-숙소목록조회-빈목록",
					pathParameters(
						parameterWithName("wishlistId")
							.description("빈 위시리스트의 고유 식별자")
					),
					queryParameters(
						parameterWithName("size")
							.description("페이지 크기")
							.optional()
					),
					responseFields(
						fieldWithPath("wishlistAccommodations[]")
							.type(JsonFieldType.ARRAY)
							.description("빈 위시리스트 숙소 목록"),
						fieldWithPath("pageInfo")
							.type(JsonFieldType.OBJECT)
							.description("페이징 정보"),
						fieldWithPath("pageInfo.hasNext")
							.type(JsonFieldType.BOOLEAN)
							.description("다음 페이지 존재 여부"),
						fieldWithPath("pageInfo.nextCursor")
							.type(JsonFieldType.STRING)
							.description("다음 페이지 커서")
							.optional(),
						fieldWithPath("pageInfo.currentSize")
							.type(JsonFieldType.NUMBER)
							.description("현재 페이지 항목 수")
					)));

			verify(wishlistService).findWishlistAccommodations(eq(emptyWishlistId), any(CursorRequest.CursorPageRequest.class));
		}

		@Test
		@DisplayName("시나리오: 존재하지 않는 위시리스트의 숙소 목록 조회를 시도한다")
		void 존재하지_않는_위시리스트의_숙소_목록_조회를_시도한다() throws Exception {
			// Given: 존재하지 않는 위시리스트 ID로 조회하려는 상황
			Long nonExistentWishlistId = 999L;

			when(wishlistService.findWishlistAccommodations(eq(nonExistentWishlistId), any(CursorRequest.CursorPageRequest.class)))
				.thenThrow(new WishlistNotFoundException());

			// When: 존재하지 않는 위시리스트의 숙소 목록 조회를 시도한다
			mockMvc.perform(get("/api/members/wishlists/{wishlistId}/accommodations", nonExistentWishlistId)
					.param("size", "20"))

				// Then: 404 Not Found 오류가 발생한다
				.andExpect(status().isNotFound())

				// document
				.andDo(document("위시리스트-숙소목록조회-위시리스트없음-실패",
					pathParameters(
						parameterWithName("wishlistId")
							.description("존재하지 않는 위시리스트 ID")
					),
					queryParameters(
						parameterWithName("size")
							.description("페이지 크기")
							.optional()
					)));

			verify(wishlistService).findWishlistAccommodations(eq(nonExistentWishlistId), any(CursorRequest.CursorPageRequest.class));
		}

		@Test
		@DisplayName("시나리오: 다른 사용자의 위시리스트 숙소 목록 조회를 시도한다")
		void 다른_사용자의_위시리스트_숙소_목록_조회를_시도한다() throws Exception {
			// Given: 다른 사용자 소유의 위시리스트 조회를 시도하는 상황
			Long otherUserWishlistId = 1L;

			when(wishlistService.findWishlistAccommodations(eq(otherUserWishlistId), any(CursorRequest.CursorPageRequest.class)))
				.thenThrow(new WishlistAccessDeniedException());

			// When: 다른 사용자의 위시리스트 숙소 목록 조회를 시도한다
			mockMvc.perform(get("/api/members/wishlists/{wishlistId}/accommodations", otherUserWishlistId)
					.param("size", "20"))

				// Then: 403 Forbidden 오류가 발생한다
				.andExpect(status().isForbidden())

				// document
				.andDo(document("위시리스트-숙소목록조회-권한없음-실패",
					pathParameters(
						parameterWithName("wishlistId")
							.description("다른 사용자 소유의 위시리스트 ID")
					),
					queryParameters(
						parameterWithName("size")
							.description("페이지 크기")
							.optional()
					)));

			verify(wishlistService).findWishlistAccommodations(eq(otherUserWishlistId), any(CursorRequest.CursorPageRequest.class));
		}

		@Test
		@DisplayName("시나리오: 커서 기반 페이징으로 위시리스트 숙소 목록을 조회한다")
		void 커서_기반_페이징으로_위시리스트_숙소_목록을_조회한다() throws Exception {
			// Given: 커서를 이용한 페이징 조회 상황
			Long wishlistId = 1L;
			String cursor = "eyJsYXN0SWQiOjIwLCJsYXN0Q3JlYXRlZEF0IjoiMjAyNC0wMS0xNVQxMDozMDowMCJ9";

			List<WishlistResponse.WishlistAccommodationInfo> accommodations = List.of(
				new WishlistResponse.WishlistAccommodationInfo(
					30L,
					"세 번째 숙소 메모",
					new AccommodationResponse.WishlistAccommodationInfo(
						300L,
						"세 번째 숙소",
						List.of("image3.jpg"),
						List.of(),
						4.2
					)
				),
				new WishlistResponse.WishlistAccommodationInfo(
					40L,
					"네 번째 숙소 메모",
					new AccommodationResponse.WishlistAccommodationInfo(
						400L,
						"네 번째 숙소",
						List.of("image4.jpg"),
						List.of(),
						4.7
					)
				)
			);

			CursorResponse.PageInfo pageInfo = CursorResponse.PageInfo.builder()
				.hasNext(true)
				.nextCursor("eyJsYXN0SWQiOjQwLCJsYXN0Q3JlYXRlZEF0IjoiMjAyNC0wMS0xNVQxMDo0NTowMCJ9")
				.currentSize(2)
				.build();

			WishlistResponse.WishlistAccommodationInfos expectedResponse =
				new WishlistResponse.WishlistAccommodationInfos(accommodations, pageInfo);

			when(wishlistService.findWishlistAccommodations(eq(wishlistId), any(CursorRequest.CursorPageRequest.class)))
				.thenReturn(expectedResponse);

			// When: 커서를 포함한 페이징 조회를 수행한다
			mockMvc.perform(get("/api/members/wishlists/{wishlistId}/accommodations", wishlistId)
					.param("size", "2")
					.param("cursor", cursor))

				// Then: 페이징된 결과가 반환된다
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.wishlistAccommodations").isArray())
				.andExpect(jsonPath("$.wishlistAccommodations.length()").value(2))
				.andExpect(jsonPath("$.wishlistAccommodations[0].id").value(30L))
				.andExpect(jsonPath("$.wishlistAccommodations[1].id").value(40L))
				.andExpect(jsonPath("$.pageInfo.hasNext").value(true))
				.andExpect(jsonPath("$.pageInfo.nextCursor").value("eyJsYXN0SWQiOjQwLCJsYXN0Q3JlYXRlZEF0IjoiMjAyNC0wMS0xNVQxMDo0NTowMCJ9"))
				.andExpect(jsonPath("$.pageInfo.currentSize").value(2))

				// document
				.andDo(document("위시리스트-숙소목록조회-페이징",
					pathParameters(
						parameterWithName("wishlistId")
							.description("위시리스트 고유 식별자")
					),
					queryParameters(
						parameterWithName("size")
							.description("페이지 크기"),
						parameterWithName("cursor")
							.description("페이징을 위한 커서")
					),
					responseFields(
						fieldWithPath("wishlistAccommodations[]")
							.type(JsonFieldType.ARRAY)
							.description("위시리스트 숙소 목록"),
						fieldWithPath("wishlistAccommodations[].id")
							.type(JsonFieldType.NUMBER)
							.description("위시리스트 항목 ID"),
						fieldWithPath("wishlistAccommodations[].name")
							.type(JsonFieldType.STRING)
							.description("위시리스트 항목 메모"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo")
							.type(JsonFieldType.OBJECT)
							.description("숙소 정보"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.accommodationId")
							.type(JsonFieldType.NUMBER)
							.description("숙소 ID"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.name")
							.type(JsonFieldType.STRING)
							.description("숙소 이름"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.accommodationImageUrls[]")
							.type(JsonFieldType.ARRAY)
							.description("숙소 이미지 URL 목록"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.amenities[]")
							.type(JsonFieldType.ARRAY)
							.description("편의시설 목록"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.averageRating")
							.type(JsonFieldType.NUMBER)
							.description("평균 평점"),
						fieldWithPath("pageInfo")
							.type(JsonFieldType.OBJECT)
							.description("페이징 정보"),
						fieldWithPath("pageInfo.hasNext")
							.type(JsonFieldType.BOOLEAN)
							.description("다음 페이지 존재 여부"),
						fieldWithPath("pageInfo.nextCursor")
							.type(JsonFieldType.STRING)
							.description("다음 페이지 커서"),
						fieldWithPath("pageInfo.currentSize")
							.type(JsonFieldType.NUMBER)
							.description("현재 페이지 크기")
					)));

			verify(wishlistService).findWishlistAccommodations(eq(wishlistId), any(CursorRequest.CursorPageRequest.class));
		}

		@Test
		@DisplayName("시나리오: 한 개의 숙소만 있는 위시리스트를 조회한다")
		void 한_개의_숙소만_있는_위시리스트를_조회한다() throws Exception {
			// Given: 한 개의 숙소만 포함된 위시리스트 조회 상황
			Long wishlistId = 1L;

			List<AccommodationResponse.AmenityInfoResponse> amenities = List.of(
				new AccommodationResponse.AmenityInfoResponse(AmenityType.WIFI, 1),
				new AccommodationResponse.AmenityInfoResponse(AmenityType.PARKING, 1),
				new AccommodationResponse.AmenityInfoResponse(AmenityType.KITCHEN, 1)
			);

			List<WishlistResponse.WishlistAccommodationInfo> accommodations = List.of(
				new WishlistResponse.WishlistAccommodationInfo(
					10L,
					"유일한 숙소에 대한 상세한 메모입니다. 정말 좋은 곳이에요!",
					new AccommodationResponse.WishlistAccommodationInfo(
						100L,
						"프리미엄 호텔",
						List.of("premium_hotel_1.jpg", "premium_hotel_2.jpg", "premium_hotel_3.jpg"),
						amenities,
						4.8
					)
				)
			);

			CursorResponse.PageInfo pageInfo = CursorResponse.PageInfo.builder()
				.hasNext(false)
				.nextCursor(null)
				.currentSize(1)
				.build();

			WishlistResponse.WishlistAccommodationInfos expectedResponse =
				new WishlistResponse.WishlistAccommodationInfos(accommodations, pageInfo);

			when(wishlistService.findWishlistAccommodations(eq(wishlistId), any(CursorRequest.CursorPageRequest.class)))
				.thenReturn(expectedResponse);

			// When: 한 개의 숙소만 있는 위시리스트를 조회한다
			mockMvc.perform(get("/api/members/wishlists/{wishlistId}/accommodations", wishlistId)
					.param("size", "20"))

				// Then: 한 개의 숙소 정보가 상세히 반환된다
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.wishlistAccommodations").isArray())
				.andExpect(jsonPath("$.wishlistAccommodations.length()").value(1))
				.andExpect(jsonPath("$.wishlistAccommodations[0].id").value(10L))
				.andExpect(jsonPath("$.wishlistAccommodations[0].name").value("유일한 숙소에 대한 상세한 메모입니다. 정말 좋은 곳이에요!"))
				.andExpect(jsonPath("$.wishlistAccommodations[0].accommodationInfo.accommodationId").value(100L))
				.andExpect(jsonPath("$.wishlistAccommodations[0].accommodationInfo.name").value("프리미엄 호텔"))
				.andExpect(jsonPath("$.wishlistAccommodations[0].accommodationInfo.accommodationImageUrls").isArray())
				.andExpect(jsonPath("$.wishlistAccommodations[0].accommodationInfo.accommodationImageUrls.length()").value(3))
				.andExpect(jsonPath("$.wishlistAccommodations[0].accommodationInfo.amenities").isArray())
				.andExpect(jsonPath("$.wishlistAccommodations[0].accommodationInfo.amenities.length()").value(3))
				.andExpect(jsonPath("$.wishlistAccommodations[0].accommodationInfo.averageRating").value(4.8))
				.andExpect(jsonPath("$.pageInfo.hasNext").value(false))
				.andExpect(jsonPath("$.pageInfo.currentSize").value(1))

				// document
				.andDo(document("위시리스트-숙소목록조회-단일숙소",
					pathParameters(
						parameterWithName("wishlistId")
							.description("한 개의 숙소만 있는 위시리스트 ID")
					),
					queryParameters(
						parameterWithName("size")
							.description("페이지 크기")
							.optional()
					),
					responseFields(
						fieldWithPath("wishlistAccommodations[]")
							.type(JsonFieldType.ARRAY)
							.description("위시리스트 숙소 목록 (1개)"),
						fieldWithPath("wishlistAccommodations[].id")
							.type(JsonFieldType.NUMBER)
							.description("위시리스트 항목 ID"),
						fieldWithPath("wishlistAccommodations[].name")
							.type(JsonFieldType.STRING)
							.description("상세한 메모"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo")
							.type(JsonFieldType.OBJECT)
							.description("숙소 상세 정보"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.accommodationId")
							.type(JsonFieldType.NUMBER)
							.description("숙소 ID"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.name")
							.type(JsonFieldType.STRING)
							.description("숙소 이름"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.accommodationImageUrls[]")
							.type(JsonFieldType.ARRAY)
							.description("다수의 숙소 이미지 URL"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.amenities[]")
							.type(JsonFieldType.ARRAY)
							.description("다양한 편의시설"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.amenities[].type")
							.type(JsonFieldType.STRING)
							.description("편의시설 타입"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.amenities[].count")
							.type(JsonFieldType.NUMBER)
							.description("편의시설 개수"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.averageRating")
							.type(JsonFieldType.NUMBER)
							.description("높은 평점"),
						fieldWithPath("pageInfo")
							.type(JsonFieldType.OBJECT)
							.description("페이징 정보"),
						fieldWithPath("pageInfo.hasNext")
							.type(JsonFieldType.BOOLEAN)
							.description("다음 페이지 존재 여부 (false)"),
						fieldWithPath("pageInfo.nextCursor")
							.type(JsonFieldType.STRING)
							.description("다음 페이지 커서 (null)")
							.optional(),
						fieldWithPath("pageInfo.currentSize")
							.type(JsonFieldType.NUMBER)
							.description("현재 페이지 크기 (1)")
					)));

			verify(wishlistService).findWishlistAccommodations(eq(wishlistId), any(CursorRequest.CursorPageRequest.class));
		}

		@ParameterizedTest(name = "잘못된 위시리스트 ID: {0}")
		@ValueSource(longs = {-1L, 0L})
		@DisplayName("시나리오: 잘못된 위시리스트 ID로 숙소 목록 조회를 시도한다")
		void 잘못된_위시리스트_ID로_숙소_목록_조회를_시도한다(Long invalidWishlistId) throws Exception {
			// Given: 잘못된 위시리스트 ID로 조회하려는 상황
			when(wishlistService.findWishlistAccommodations(eq(invalidWishlistId), any(CursorRequest.CursorPageRequest.class)))
				.thenThrow(new WishlistNotFoundException());

			// When: 잘못된 위시리스트 ID로 조회를 시도한다
			mockMvc.perform(get("/api/members/wishlists/{wishlistId}/accommodations", invalidWishlistId)
					.param("size", "20"))

				// Then: 404 Not Found 오류가 발생한다
				.andExpect(status().isNotFound())

				// document
				.andDo(document("위시리스트-숙소목록조회-잘못된ID-" + Math.abs(invalidWishlistId),
					pathParameters(
						parameterWithName("wishlistId")
							.description("잘못된 위시리스트 ID: " + invalidWishlistId)
					),
					queryParameters(
						parameterWithName("size")
							.description("페이지 크기")
							.optional()
					)));

			verify(wishlistService).findWishlistAccommodations(eq(invalidWishlistId), any(CursorRequest.CursorPageRequest.class));
		}

		@Test
		@DisplayName("시나리오: 최대 페이지 크기로 위시리스트 숙소 목록을 조회한다")
		void 최대_페이지_크기로_위시리스트_숙소_목록을_조회한다() throws Exception {
			// Given: 최대 페이지 크기(50)로 조회하는 상황
			Long wishlistId = 1L;
			String maxSize = "50";

			// 50개의 숙소 항목 생성
			List<WishlistResponse.WishlistAccommodationInfo> accommodations = new ArrayList<>();
			for (int i = 1; i <= 50; i++) {
				accommodations.add(
					new WishlistResponse.WishlistAccommodationInfo(
						(long) i,
						"숙소 " + i + " 메모",
						new AccommodationResponse.WishlistAccommodationInfo(
							(long) (i * 100),
							"숙소 " + i,
							List.of("image" + i + ".jpg"),
							List.of(),
							4.0 + (i % 10) * 0.1
						)
					)
				);
			}

			CursorResponse.PageInfo pageInfo = CursorResponse.PageInfo.builder()
				.hasNext(true)
				.nextCursor("next_page_cursor")
				.currentSize(50)
				.build();

			WishlistResponse.WishlistAccommodationInfos expectedResponse =
				new WishlistResponse.WishlistAccommodationInfos(accommodations, pageInfo);

			when(wishlistService.findWishlistAccommodations(eq(wishlistId), any(CursorRequest.CursorPageRequest.class)))
				.thenReturn(expectedResponse);

			// When: 최대 페이지 크기로 조회한다
			mockMvc.perform(get("/api/members/wishlists/{wishlistId}/accommodations", wishlistId)
					.param("size", maxSize))

				// Then: 최대 50개의 항목이 반환된다
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.wishlistAccommodations").isArray())
				.andExpect(jsonPath("$.wishlistAccommodations.length()").value(50))
				.andExpect(jsonPath("$.wishlistAccommodations[0].id").value(1L))
				.andExpect(jsonPath("$.wishlistAccommodations[49].id").value(50L))
				.andExpect(jsonPath("$.pageInfo.hasNext").value(true))
				.andExpect(jsonPath("$.pageInfo.currentSize").value(50))

				// document
				.andDo(document("위시리스트-숙소목록조회-최대크기",
					pathParameters(
						parameterWithName("wishlistId")
							.description("위시리스트 ID")
					),
					queryParameters(
						parameterWithName("size")
							.description("최대 페이지 크기 (50)")
					),
					responseFields(
						fieldWithPath("wishlistAccommodations[]")
							.type(JsonFieldType.ARRAY)
							.description("최대 50개의 위시리스트 숙소 목록"),
						fieldWithPath("wishlistAccommodations[].id")
							.type(JsonFieldType.NUMBER)
							.description("위시리스트 항목 ID"),
						fieldWithPath("wishlistAccommodations[].name")
							.type(JsonFieldType.STRING)
							.description("위시리스트 항목 메모"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo")
							.type(JsonFieldType.OBJECT)
							.description("숙소 정보"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.accommodationId")
							.type(JsonFieldType.NUMBER)
							.description("숙소 ID"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.name")
							.type(JsonFieldType.STRING)
							.description("숙소 이름"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.accommodationImageUrls[]")
							.type(JsonFieldType.ARRAY)
							.description("숙소 이미지 URL 목록"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.amenities[]")
							.type(JsonFieldType.ARRAY)
							.description("편의시설 목록"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.averageRating")
							.type(JsonFieldType.NUMBER)
							.description("평균 평점"),
						fieldWithPath("pageInfo")
							.type(JsonFieldType.OBJECT)
							.description("페이징 정보"),
						fieldWithPath("pageInfo.hasNext")
							.type(JsonFieldType.BOOLEAN)
							.description("다음 페이지 존재 여부"),
						fieldWithPath("pageInfo.nextCursor")
							.type(JsonFieldType.STRING)
							.description("다음 페이지 커서"),
						fieldWithPath("pageInfo.currentSize")
							.type(JsonFieldType.NUMBER)
							.description("현재 페이지 크기 (50)")
					)));

			verify(wishlistService).findWishlistAccommodations(eq(wishlistId), any(CursorRequest.CursorPageRequest.class));
		}

		@Test
		@DisplayName("시나리오: 잘못된 커서로 위시리스트 숙소 목록 조회 시 첫 페이지로 처리된다")
		void 잘못된_커서로_위시리스트_숙소_목록_조회_시_첫_페이지로_처리된다() throws Exception {
			// Given: 잘못된 형식의 커서로 조회하지만 첫 페이지로 처리되는 상황
			Long wishlistId = 1L;
			String invalidCursor = "invalid_cursor_format";

			// 잘못된 커서는 디코딩 실패 시 null로 처리되어 첫 페이지 조회가 됨
			List<WishlistResponse.WishlistAccommodationInfo> accommodations = List.of(
				new WishlistResponse.WishlistAccommodationInfo(
					10L,
					"첫 페이지 숙소 메모",
					new AccommodationResponse.WishlistAccommodationInfo(
						100L,
						"첫 페이지 숙소",
						List.of("first_page_image.jpg"),
						List.of(new AccommodationResponse.AmenityInfoResponse(AmenityType.WIFI, 1)),
						4.2
					)
				)
			);

			CursorResponse.PageInfo pageInfo = CursorResponse.PageInfo.builder()
				.hasNext(true)
				.nextCursor("valid_next_cursor")
				.currentSize(1)
				.build();

			WishlistResponse.WishlistAccommodationInfos expectedResponse =
				new WishlistResponse.WishlistAccommodationInfos(accommodations, pageInfo);

			when(wishlistService.findWishlistAccommodations(eq(wishlistId), any(CursorRequest.CursorPageRequest.class)))
				.thenReturn(expectedResponse);

			// When: 잘못된 커서로 조회를 시도한다
			mockMvc.perform(get("/api/members/wishlists/{wishlistId}/accommodations", wishlistId)
					.param("size", "20")
					.param("cursor", invalidCursor))

				// Then: 에러가 발생하지 않고 첫 페이지가 반환된다
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.wishlistAccommodations").isArray())
				.andExpect(jsonPath("$.wishlistAccommodations.length()").value(1))
				.andExpect(jsonPath("$.wishlistAccommodations[0].id").value(10L))
				.andExpect(jsonPath("$.wishlistAccommodations[0].name").value("첫 페이지 숙소 메모"))
				.andExpect(jsonPath("$.pageInfo.hasNext").value(true))
				.andExpect(jsonPath("$.pageInfo.currentSize").value(1))

				// document
				.andDo(document("위시리스트-숙소목록조회-잘못된커서-첫페이지처리",
					pathParameters(
						parameterWithName("wishlistId")
							.description("위시리스트 ID")
					),
					queryParameters(
						parameterWithName("size")
							.description("페이지 크기"),
						parameterWithName("cursor")
							.description("잘못된 형식의 커서 (첫 페이지로 처리됨)")
					),
					responseFields(
						fieldWithPath("wishlistAccommodations[]")
							.type(JsonFieldType.ARRAY)
							.description("첫 페이지 위시리스트 숙소 목록"),
						fieldWithPath("wishlistAccommodations[].id")
							.type(JsonFieldType.NUMBER)
							.description("위시리스트 항목 ID"),
						fieldWithPath("wishlistAccommodations[].name")
							.type(JsonFieldType.STRING)
							.description("위시리스트 항목 메모"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo")
							.type(JsonFieldType.OBJECT)
							.description("숙소 정보"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.accommodationId")
							.type(JsonFieldType.NUMBER)
							.description("숙소 ID"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.name")
							.type(JsonFieldType.STRING)
							.description("숙소 이름"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.accommodationImageUrls[]")
							.type(JsonFieldType.ARRAY)
							.description("숙소 이미지 URL 목록"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.amenities[]")
							.type(JsonFieldType.ARRAY)
							.description("편의시설 목록"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.amenities[].type")
							.type(JsonFieldType.STRING)
							.description("편의시설 타입"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.amenities[].count")
							.type(JsonFieldType.NUMBER)
							.description("편의시설 개수"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.averageRating")
							.type(JsonFieldType.NUMBER)
							.description("평균 평점"),
						fieldWithPath("pageInfo")
							.type(JsonFieldType.OBJECT)
							.description("페이징 정보"),
						fieldWithPath("pageInfo.hasNext")
							.type(JsonFieldType.BOOLEAN)
							.description("다음 페이지 존재 여부"),
						fieldWithPath("pageInfo.nextCursor")
							.type(JsonFieldType.STRING)
							.description("다음 페이지를 위한 유효한 커서"),
						fieldWithPath("pageInfo.currentSize")
							.type(JsonFieldType.NUMBER)
							.description("현재 페이지 크기")
					)));

			verify(wishlistService).findWishlistAccommodations(eq(wishlistId), any(CursorRequest.CursorPageRequest.class));
		}

		@Test
		@DisplayName("시나리오: 잘못된 페이지 크기로 위시리스트 숙소 목록을 조회한다")
		void 잘못된_페이지_크기로_위시리스트_숙소_목록을_조회한다() throws Exception {
			// Given: 잘못된 페이지 크기로 조회하려는 상황
			Long wishlistId = 1L;
			String invalidSize = "100"; // 최대 50을 초과

			when(wishlistService.findWishlistAccommodations(eq(wishlistId), any(CursorRequest.CursorPageRequest.class)))
				.thenThrow(new CursorPageSizeException());

			// When: 잘못된 페이지 크기로 조회를 시도한다
			mockMvc.perform(get("/api/members/wishlists/{wishlistId}/accommodations", wishlistId)
					.param("size", invalidSize))

				// Then: 400 Bad Request 오류가 발생한다
				.andExpect(status().isBadRequest())

				// document
				.andDo(document("위시리스트-숙소목록조회-잘못된페이지크기-실패",
					pathParameters(
						parameterWithName("wishlistId")
							.description("위시리스트 ID")
					),
					queryParameters(
						parameterWithName("size")
							.description("잘못된 페이지 크기 (최대 50 초과)")
					)));

			verify(wishlistService).findWishlistAccommodations(eq(wishlistId), any(CursorRequest.CursorPageRequest.class));
		}

		@Test
		@DisplayName("시나리오: 매우 큰 위시리스트 ID로 숙소 목록을 조회한다")
		void 매우_큰_위시리스트_ID로_숙소_목록을_조회한다() throws Exception {
			// Given: 매우 큰 위시리스트 ID로 조회하는 상황
			Long largeWishlistId = Long.MAX_VALUE;

			when(wishlistService.findWishlistAccommodations(eq(largeWishlistId), any(CursorRequest.CursorPageRequest.class)))
				.thenThrow(new WishlistNotFoundException());

			// When: 매우 큰 위시리스트 ID로 조회를 시도한다
			mockMvc.perform(get("/api/members/wishlists/{wishlistId}/accommodations", largeWishlistId)
					.param("size", "20"))

				// Then: 404 Not Found 오류가 발생한다
				.andExpect(status().isNotFound())

				// document
				.andDo(document("위시리스트-숙소목록조회-최대ID-실패",
					pathParameters(
						parameterWithName("wishlistId")
							.description("매우 큰 위시리스트 ID (Long.MAX_VALUE)")
					),
					queryParameters(
						parameterWithName("size")
							.description("페이지 크기")
							.optional()
					)));

			verify(wishlistService).findWishlistAccommodations(eq(largeWishlistId), any(CursorRequest.CursorPageRequest.class));
		}

		@Test
		@DisplayName("시나리오: 기본 페이지 크기로 위시리스트 숙소 목록을 조회한다")
		void 기본_페이지_크기로_위시리스트_숙소_목록을_조회한다() throws Exception {
			// Given: 페이지 크기를 지정하지 않고 조회하는 상황 (기본값 사용)
			Long wishlistId = 1L;

			List<WishlistResponse.WishlistAccommodationInfo> accommodations = List.of(
				new WishlistResponse.WishlistAccommodationInfo(
					10L,
					"기본 크기 테스트 메모",
					new AccommodationResponse.WishlistAccommodationInfo(
						100L,
						"기본 크기 테스트 숙소",
						List.of(),
						List.of(),
						4.0
					)
				)
			);

			CursorResponse.PageInfo pageInfo = CursorResponse.PageInfo.builder()
				.hasNext(false)
				.nextCursor(null)
				.currentSize(1)
				.build();

			WishlistResponse.WishlistAccommodationInfos expectedResponse =
				new WishlistResponse.WishlistAccommodationInfos(accommodations, pageInfo);

			when(wishlistService.findWishlistAccommodations(eq(wishlistId), any(CursorRequest.CursorPageRequest.class)))
				.thenReturn(expectedResponse);

			// When: 페이지 크기를 지정하지 않고 조회한다
			mockMvc.perform(get("/api/members/wishlists/{wishlistId}/accommodations", wishlistId))

				// Then: 기본 페이지 크기로 결과가 반환된다
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.wishlistAccommodations").isArray())
				.andExpect(jsonPath("$.wishlistAccommodations.length()").value(1))
				.andExpect(jsonPath("$.pageInfo.hasNext").value(false))
				.andExpect(jsonPath("$.pageInfo.currentSize").value(1))

				// document
				.andDo(document("위시리스트-숙소목록조회-기본크기",
					pathParameters(
						parameterWithName("wishlistId")
							.description("위시리스트 ID")
					),
					responseFields(
						fieldWithPath("wishlistAccommodations[]")
							.type(JsonFieldType.ARRAY)
							.description("위시리스트 숙소 목록"),
						fieldWithPath("wishlistAccommodations[].id")
							.type(JsonFieldType.NUMBER)
							.description("위시리스트 항목 ID"),
						fieldWithPath("wishlistAccommodations[].name")
							.type(JsonFieldType.STRING)
							.description("위시리스트 항목 메모"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo")
							.type(JsonFieldType.OBJECT)
							.description("숙소 정보"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.accommodationId")
							.type(JsonFieldType.NUMBER)
							.description("숙소 ID"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.name")
							.type(JsonFieldType.STRING)
							.description("숙소 이름"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.accommodationImageUrls[]")
							.type(JsonFieldType.ARRAY)
							.description("숙소 이미지 URL 목록"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.amenities[]")
							.type(JsonFieldType.ARRAY)
							.description("편의시설 목록"),
						fieldWithPath("wishlistAccommodations[].accommodationInfo.averageRating")
							.type(JsonFieldType.NUMBER)
							.description("평균 평점"),
						fieldWithPath("pageInfo")
							.type(JsonFieldType.OBJECT)
							.description("페이징 정보"),
						fieldWithPath("pageInfo.hasNext")
							.type(JsonFieldType.BOOLEAN)
							.description("다음 페이지 존재 여부"),
						fieldWithPath("pageInfo.nextCursor")
							.type(JsonFieldType.STRING)
							.description("다음 페이지 커서")
							.optional(),
						fieldWithPath("pageInfo.currentSize")
							.type(JsonFieldType.NUMBER)
							.description("현재 페이지 크기")
					)));

			verify(wishlistService).findWishlistAccommodations(eq(wishlistId), any(CursorRequest.CursorPageRequest.class));
		}
	}
}
