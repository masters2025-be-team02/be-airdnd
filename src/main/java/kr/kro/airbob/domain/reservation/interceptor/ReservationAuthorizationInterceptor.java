package kr.kro.airbob.domain.reservation.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.kro.airbob.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ReservationAuthorizationInterceptor implements HandlerInterceptor {

    private final ReservationRepository reservationRepository;

    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws IOException {

        String method = request.getMethod();
        String uri = request.getRequestURI();

        // URI에서 예약 ID 추출 (예: /api/reservations/accommodations/123)
        String[] reservationSegments = uri.split("/");
        Long reservationId;
        try{
            reservationId = Long.parseLong(reservationSegments[reservationSegments.length - 1]);
        }catch (NumberFormatException e){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "유효하지 않은 예약 ID입니다.");
            return false;
        }

        // URI에서 숙소 ID 추출 (예: /api/reservations/accommodations/123)
        String[] accommodationSegments = uri.split("/");
        Long accommodationId;
        try{
            accommodationId = Long.parseLong(accommodationSegments[accommodationSegments.length - 1]);
        }catch (NumberFormatException e){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "유효하지 않은 숙소 ID입니다.");
            return false;
        }

        // 수정/삭제가 아니면 통과
        if (!(method.equals("POST") || method.equals("DELETE"))) {
            return true;
        }

        // 필터에서 저장한 memberId 가져오기
        Long requestMemberId = (Long) request.getAttribute("memberId");

        // 예약 정보 조회 및 예약자 검증
        Long reservationMemberId = reservationRepository.findMemberIdById(reservationId).orElse(null);

        if(reservationMemberId == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "해당 예약을 찾을 수 없습니다.");
            return false;
        }

        if(!reservationMemberId.equals(requestMemberId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "해당 예약은 예약자만 .");
            return false;
        }

        return true;
    }

}
