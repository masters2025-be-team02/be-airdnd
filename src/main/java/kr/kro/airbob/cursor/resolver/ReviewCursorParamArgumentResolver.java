package kr.kro.airbob.cursor.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import kr.kro.airbob.cursor.annotation.CursorParam;
import kr.kro.airbob.cursor.dto.CursorRequest;
import kr.kro.airbob.cursor.dto.CursorResponse;
import kr.kro.airbob.cursor.util.CursorDecoder;

@Component
public class ReviewCursorParamArgumentResolver extends CursorParamArgumentResolver {

	public ReviewCursorParamArgumentResolver(CursorDecoder cursorDecoder) {
		super(cursorDecoder);
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(CursorParam.class) &&
			parameter.getParameterType().equals(CursorRequest.ReviewCursorPageRequest.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

		CursorParam annotation = parameter.getParameterAnnotation(CursorParam.class);

		int size = parseSize(webRequest, annotation);
		CursorResponse.CursorData cursorData = parseCursorData(webRequest, annotation);

		// ReviewCursorData로 캐스팅 시도
		CursorResponse.ReviewCursorData reviewCursorData = null;
		if (cursorData instanceof CursorResponse.ReviewCursorData) {
			reviewCursorData = (CursorResponse.ReviewCursorData) cursorData;
		} else if (cursorData != null) {
			// 일반 CursorData면 ReviewCursorData로 변환하되 lastRating은 null
			reviewCursorData = new CursorResponse.ReviewCursorData(
				cursorData.getId(),
				cursorData.getLastCreatedAt(),
				null
			);
		}

		return CursorRequest.ReviewCursorPageRequest.builder()
			.size(size)
			.lastId(reviewCursorData != null ? reviewCursorData.getId() : null)
			.lastCreatedAt(reviewCursorData != null ? reviewCursorData.getLastCreatedAt() : null)
			.lastRating(reviewCursorData != null ? reviewCursorData.getLastRating() : null)
			.build();
	}
}
