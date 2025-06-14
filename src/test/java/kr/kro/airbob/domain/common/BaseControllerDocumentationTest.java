package kr.kro.airbob.domain.common;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.kro.airbob.common.exception.GlobalExceptionHandler;

@ExtendWith(RestDocumentationExtension.class)
public abstract class BaseControllerDocumentationTest {

	protected MockMvc mockMvc;

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
