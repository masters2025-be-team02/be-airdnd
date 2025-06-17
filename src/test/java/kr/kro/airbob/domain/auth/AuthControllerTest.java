package kr.kro.airbob.domain.auth;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.cookies.CookieDocumentation.responseCookies;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import kr.kro.airbob.cursor.util.CursorDecoder;
import kr.kro.airbob.domain.accommodation.interceptor.AccommodationAuthorizationInterceptor;
import kr.kro.airbob.domain.auth.dto.AuthRequestDto.LoginRequest;
import kr.kro.airbob.domain.common.BaseControllerDocumentationTest;
import kr.kro.airbob.domain.wishlist.interceptor.WishlistAuthorizationInterceptor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureRestDocs
@WebMvcTest(AuthController.class)
class AuthControllerTest extends BaseControllerDocumentationTest {

    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private CursorDecoder cursorDecoder;
    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;
    @MockitoBean
    private AccommodationAuthorizationInterceptor accommodationAuthorizationInterceptor;

    @MockitoBean
    private AccommodationAuthorizationInterceptor accommodationAuthorizationInterceptor;

    @MockitoBean
    private WishlistAuthorizationInterceptor wishlistAuthorizationInterceptor;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthController authController;

    @Override
    protected Object getController() {
        return authController;
    }
    @Test
    @DisplayName("로그인 요청 시 세션 쿠키가 발급되어야 한다")
    void login() throws Exception {
        // given
        String email = "user@example.com";
        String password = "secure123!";
        String sessionId = "test-session-id";

        given(authService.login(eq(email), eq(password))).willReturn(sessionId);

        LoginRequest request = new LoginRequest(email, password);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().value("SESSION_ID", sessionId))
                .andDo(document("login",
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("회원 이메일"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                        ),
                        responseCookies(
                                cookieWithName("SESSION_ID").description("서버가 발급한 세션 식별자 쿠키")
                        )
                ));
    }

    @Test
    @DisplayName("로그아웃 요청 시 세션이 제거되어야 한다")
    void logout() throws Exception {
        // given
        String sessionId = "test-session-id";

        willDoNothing().given(authService).logout(sessionId);

        // when & then
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie("SESSION_ID", sessionId)))
                .andExpect(status().isNoContent())
                .andDo(document("logout",
                        requestCookies(
                                cookieWithName("SESSION_ID").description("클라이언트가 보낸 세션 식별자")
                        )
                ));
    }


}
