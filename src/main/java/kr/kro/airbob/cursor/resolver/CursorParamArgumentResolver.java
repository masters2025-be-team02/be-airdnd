package kr.kro.airbob.cursor.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import kr.kro.airbob.cursor.annotation.CursorParam;
import kr.kro.airbob.cursor.dto.CursorRequest;
import kr.kro.airbob.cursor.dto.CursorResponse;
import kr.kro.airbob.cursor.exception.CursorPageSizeException;
import kr.kro.airbob.cursor.util.CursorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class CursorParamArgumentResolver implements HandlerMethodArgumentResolver {

	private final CursorDecoder cursorDecoder;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(CursorParam.class) &&
			parameter.getParameterType().equals(CursorRequest.CursorPageRequest.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

		CursorParam annotation = parameter.getParameterAnnotation(CursorParam.class);

		// size
		String sizeParam = webRequest.getParameter(annotation.sizeParam());
		int size = sizeParam != null ? Integer.parseInt(sizeParam) : annotation.defaultSize();

		if (size < 1) {
			throw new CursorPageSizeException("커서 페이지 크기는 1 이상이여야 합니다.");
		}

		String cursorParam = webRequest.getParameter(annotation.cursorParam());
		CursorResponse.CursorData cursorData = cursorDecoder.decode(cursorParam);

		return CursorRequest.CursorPageRequest.builder()
			.size(size)
			.lastId(cursorData != null ? cursorData.id() : null)
			.lastCreatedAt(cursorData != null ? cursorData.lastCreatedAt() : null)
			.build();
	}
}
