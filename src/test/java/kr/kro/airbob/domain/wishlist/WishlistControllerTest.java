package kr.kro.airbob.domain.wishlist;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import kr.kro.airbob.cursor.dto.CursorRequest;
import kr.kro.airbob.cursor.dto.CursorResponse;
import kr.kro.airbob.cursor.exception.CursorPageSizeException;
import kr.kro.airbob.cursor.resolver.CursorParamArgumentResolver;
import kr.kro.airbob.cursor.util.CursorDecoder;
import kr.kro.airbob.cursor.util.CursorEncoder;
import kr.kro.airbob.cursor.util.CursorPageInfoCreator;
import kr.kro.airbob.domain.common.BaseControllerDocumentationTest;
import kr.kro.airbob.domain.wishlist.api.WishlistController;
import kr.kro.airbob.domain.wishlist.dto.WishlistRequest;
import kr.kro.airbob.domain.wishlist.dto.WishlistResponse;
import kr.kro.airbob.domain.wishlist.exception.WishlistAccessDeniedException;
import kr.kro.airbob.domain.wishlist.exception.WishlistNotFoundException;

@WebMvcTest(WishlistController.class)
@DisplayName("위시리스트 관리 API 테스트")
class WishlistControllerTest extends BaseControllerDocumentationTest {

	@MockitoBean
	private WishlistService wishlistService;

	@MockitoBean
	private CursorParamArgumentResolver cursorParamArgumentResolver;

	@MockitoBean
	private CursorDecoder cursorDecoder;

	@MockitoBean
	private CursorEncoder cursorEncoder;

	@MockitoBean
	private CursorPageInfoCreator cursorPageInfoCreator;


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

	@Nested
	@DisplayName("위시리스트 수정:")
	class UpdateWishlistTests {

		@Test
		@DisplayName("시나리오: 사용자가 위시리스트의 이름을 수정한다")
		void 사용자가_위시리스트의_이름을_수정한다() throws Exception {
			// Given: 존재하는 위시리스트의 이름을 변경하려는 상황
			Long wishlistId = 1L;
			WishlistRequest.updateRequest request = new WishlistRequest.updateRequest("수정된 서울 여행 계획");
			WishlistResponse.updateResponse expectedResponse = new WishlistResponse.updateResponse(wishlistId);

			when(wishlistService.updateWishlist(eq(wishlistId), any(WishlistRequest.updateRequest.class), eq(1L)))
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

			verify(wishlistService).updateWishlist(eq(wishlistId), any(WishlistRequest.updateRequest.class), eq(1L));
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

			when(wishlistService.updateWishlist(eq(nonExistentWishlistId), any(WishlistRequest.updateRequest.class),
				eq(1L)))
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

			when(wishlistService.updateWishlist(eq(otherMemberWishlistId), any(WishlistRequest.updateRequest.class),
				eq(1L)))
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

			doNothing().when(wishlistService).deleteWishlist(eq(wishlistId), eq(1L));

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
			verify(wishlistService).deleteWishlist(eq(wishlistId), eq(1L));
		}

		@Test
		@DisplayName("시나리오: 여러 위시리스트를 연속으로 삭제한다")
		void 여러_위시리스트를_연속으로_삭제한다() throws Exception {
			// Given: 여러 개의 위시리스트 ID들
			Long[] wishlistIds = {1L, 2L, 3L, 4L, 5L};

			for (int i = 0; i < wishlistIds.length; i++) {
				Long wishlistId = wishlistIds[i];

				doNothing().when(wishlistService).deleteWishlist(eq(wishlistId), eq(1L));

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
				.when(wishlistService).deleteWishlist(eq(nonExistentWishlistId), eq(1L));

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
				.when(wishlistService).deleteWishlist(eq(otherMemberWishlistId), eq(1L));

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
					.param("size", "20"))

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

				// document
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
					.param("cursor", "eyJpZCI6MywiY3JlYXRlZEF0IjoiMjAyMS0wNS0xN1QwODowMDowMCJ9"))

				// Then: 페이징된 위시리스트 목록이 반환된다
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.wishlists.length()").value(2))
				.andExpect(jsonPath("$.pageInfo.hasNext").value(true))
				.andExpect(jsonPath("$.pageInfo.nextCursor").isNotEmpty())

				// document
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
			mockMvc.perform(get("/api/members/wishlists"))

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
		@DisplayName("시나리오: 잘못된 페이지 크기로 위시리스트를 조회한다")
		void 잘못된_페이지_크기로_위시리스트를_조회한다() throws Exception {
			// Given: 잘못된 페이지 크기 (0 또는 음수)
			when(wishlistService.findWishlists(any(CursorRequest.CursorPageRequest.class), eq(1L)))
				.thenThrow(new CursorPageSizeException());

			// When & Then: 잘못된 페이지 크기로 조회 시 오류 발생
			mockMvc.perform(get("/api/members/wishlists")
					.param("size", "0"))
				.andExpect(status().isBadRequest())
				.andDo(document("위시리스트-조회-잘못된크기-실패",
					queryParameters(
						parameterWithName("size")
							.description("잘못된 페이지 크기 (0 이하)")
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
					.param("cursor", invalidCursor))
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
							.description("대표 이미지 URL")
							.optional(),
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
							.description("현재 페이지의 아이템 수")
					)));
		}
	}
}
