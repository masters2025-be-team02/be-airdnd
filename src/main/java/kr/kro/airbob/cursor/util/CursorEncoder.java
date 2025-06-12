package kr.kro.airbob.cursor.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.kro.airbob.cursor.dto.CursorResponse;
import kr.kro.airbob.cursor.exception.CursorException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CursorEncoder {

	private final ObjectMapper objectMapper;

	public String encode(CursorResponse.CursorData cursor) {
		if (cursor == null) {
			return null;
		}

		try {
			String json = objectMapper.writeValueAsString(cursor);
			return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
		} catch (JsonProcessingException e) {
			throw new CursorException("커서 인코딩 실패: " + e.getMessage());
		}
	}
}
