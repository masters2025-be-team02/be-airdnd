package kr.kro.airbob.domain.common;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.kro.airbob.common.exception.GlobalExceptionHandler;
import kr.kro.airbob.cursor.resolver.CursorParamArgumentResolver;
import kr.kro.airbob.cursor.util.CursorDecoder;
import kr.kro.airbob.cursor.util.CursorEncoder;
import kr.kro.airbob.cursor.util.CursorPageInfoCreator;
import kr.kro.airbob.domain.accommodation.interceptor.AccommodationAuthorizationInterceptor;
import kr.kro.airbob.domain.recentlyViewed.interceptor.RecentlyViewedAuthorizationInterceptor;
import kr.kro.airbob.domain.review.interceptor.ReviewAuthorizationInterceptor;
import kr.kro.airbob.domain.wishlist.interceptor.WishlistAuthorizationInterceptor;

@ExtendWith(RestDocumentationExtension.class)
public abstract class BaseControllerDocumentationTest {

	protected MockMvc mockMvc;

	@MockitoBean
	private CursorParamArgumentResolver cursorParamArgumentResolver;

	@MockitoBean
	private CursorDecoder cursorDecoder;

	@MockitoBean
	private CursorEncoder cursorEncoder;

	@MockitoBean
	private CursorPageInfoCreator cursorPageInfoCreator;

	@MockitoBean
	private RedisTemplate<String, Object> redisTemplate;

	@MockitoBean
	private AccommodationAuthorizationInterceptor accommodationAuthorizationInterceptor;

	@MockitoBean
	private WishlistAuthorizationInterceptor wishlistAuthorizationInterceptor;

	@MockitoBean
	private RecentlyViewedAuthorizationInterceptor recentlyViewedAuthorizationInterceptor;

	@MockitoBean
	private ReviewAuthorizationInterceptor reviewAuthorizationInterceptor;

	@Autowired
	protected ObjectMapper objectMapper;

	@BeforeEach
	void setUp(RestDocumentationContextProvider restDocumentation) {
		this.mockMvc = MockMvcBuilders
			.standaloneSetup(getController())  // standaloneSetup 사용
			.setControllerAdvice(new GlobalExceptionHandler())
			.apply(documentationConfiguration(restDocumentation)
				.operationPreprocessors()
				.withRequestDefaults(prettyPrint())
				.withResponseDefaults(prettyPrint())
				.and()
				.uris()
				.withScheme("https")
				.withHost("api.airbob.kro.kr")
				.withPort(443))
			// .alwaysDo(document("{class-name}/{method-name}"))
			.build();
	}

	protected abstract Object getController();
}
