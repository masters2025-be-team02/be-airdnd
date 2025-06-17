package kr.kro.airbob.domain.accommodation.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import kr.kro.airbob.domain.accommodation.repository.AccommodationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AccommodationAuthorizationInterceptor implements HandlerInterceptor {

    private final AccommodationRepository accommodationRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {

        String method = request.getMethod();
        String uri = request.getRequestURI();

        if (!(method.equals("PATCH") || method.equals("DELETE"))) {
            return true; // 수정/삭제가 아니면 통과
        }

        // URI에서 숙소 ID 추출 (예: /api/accommodations/123)
        String[] segments = uri.split("/");
        Long accommodationId;
        try {
            accommodationId = Long.parseLong(segments[segments.length - 1]);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "유효하지 않은 숙소 ID입니다.");
            return false;
        }

        // 필터에서 저장한 memberId 가져오기
        Long requestMemberId = (Long) request.getAttribute("memberId");
        if (requestMemberId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
            return false;
        }

        // 숙소 정보 조회 및 작성자 검증
        Long writerId = accommodationRepository.findHostIdByAccommodationId(accommodationId).orElse(null);

        if (writerId == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "숙소를 찾을 수 없습니다.");
            return false;
        }

        if (!writerId.equals(requestMemberId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "해당 숙소는 작성자만 .");
            return false;
        }

        return true;
    }
}
