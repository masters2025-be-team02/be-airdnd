package kr.kro.airbob.geo;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class ClientIpExtractor {

	public static final String X_FORWARDED_FOR = "X-Forwarded-For";
	public static final String COMMA = ",";
	public static final String X_REAL_IP = "X-Real-IP";

	public String extractClientIp(HttpServletRequest request) {
		// X-Forwarded-For 헤더 확인
		String xForwardedFor = request.getHeader(X_FORWARDED_FOR);
		if (isValidIp(xForwardedFor)) {
			return xForwardedFor.split(COMMA)[0].trim();
		}

		// X-Real-IP 헤더 확인
		String xRealIp = request.getHeader(X_REAL_IP);
		if (isValidIp(xRealIp)) {
			return xRealIp;
		}

		// 둘 다 없으면 null 반환
		return null;
	}

	private boolean isValidIp(String ip) {
		return ip != null &&
			!ip.trim().isEmpty() &&
			!"unknown".equalsIgnoreCase(ip);
	}
}
