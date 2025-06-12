package kr.kro.airbob.cursor.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.kro.airbob.cursor.dto.CursorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class CursorDecoder {

	private final ObjectMapper objectMapper;

	public CursorResponse.CursorData decode(String cursor) {
		if (cursor == null || cursor.isBlank()) {
			return null;
		}

		try {
			String decoded = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
			return objectMapper.readValue(decoded, CursorResponse.CursorData.class);
		} catch (Exception e) {
			log.warn("커서 디코딩 실패: {}", e.getMessage());
			return null; // 디코딩 실패 시 첫 페이지
		}
	}
}
