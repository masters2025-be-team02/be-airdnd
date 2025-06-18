package kr.kro.airbob.config;

import java.util.List;

import kr.kro.airbob.domain.accommodation.interceptor.AccommodationAuthorizationInterceptor;
import kr.kro.airbob.domain.auth.filter.SessionAuthFilter;
import kr.kro.airbob.domain.recentlyViewed.interceptor.RecentlyViewedAuthorizationInterceptor;
import kr.kro.airbob.domain.wishlist.interceptor.WishlistAuthorizationInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import kr.kro.airbob.cursor.resolver.CursorParamArgumentResolver;
import lombok.RequiredArgsConstructor;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

	private final CursorParamArgumentResolver cursorParamArgumentResolver;
	private final SessionAuthFilter sessionAuthFilter;
	private final AccommodationAuthorizationInterceptor interceptor;
	private final WishlistAuthorizationInterceptor wishlistInterceptor;
	private final RecentlyViewedAuthorizationInterceptor recentlyViewedInterceptor;
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(cursorParamArgumentResolver);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(interceptor)
				.addPathPatterns("/api/accommodations/**"); // 적용 경로

		registry.addInterceptor(wishlistInterceptor)
			.addPathPatterns("/api/members/wishlists/**");

		registry.addInterceptor(recentlyViewedInterceptor)
			.addPathPatterns("/api/members/recentlyViewed/**");
	}

	@Bean
	public FilterRegistrationBean<SessionAuthFilter> sessionFilter() {
		log.info("sessionFilter");
		FilterRegistrationBean<SessionAuthFilter> bean = new FilterRegistrationBean<>(sessionAuthFilter);
		bean.addUrlPatterns("/api/accommodations", "/api/accommodations/*",
			"/api/members/wishlists", "/api/members/wishlists/*",
			"/api/members/recentlyViewed", "/api/members/recentlyViewed/*");
		bean.setOrder(1);
		return bean;
	}
}
