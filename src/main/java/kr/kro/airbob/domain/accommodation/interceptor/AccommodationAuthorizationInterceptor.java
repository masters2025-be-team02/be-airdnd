package kr.kro.airbob.domain.accommodation.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import kr.kro.airbob.domain.accommodation.repository.AccommodationRepository;
import kr.kro.airbob.domain.wishlist.repository.WishlistAccommodationRepository;
import kr.kro.airbob.domain.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AccommodationAuthorizationInterceptor implements HandlerInterceptor {

    public static final int WISHLIST_ID_INDEX = 4;
    public static final int WISHLIST_ACCOMMODATION_ID_INDEX = 6;
    private final WishlistRepository wishlistRepository;
    private final WishlistAccommodationRepository wishlistAccommodationRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // 모든 요청에서 memberId 확인 및 설정
        Long requestMemberId = (Long)request.getAttribute("memberId");
        if (requestMemberId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
            return false;
        }

        // 위시리스트 생성, 조회는 memberId 설정 후 통과
        if ((method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("GET"))
            && uri.matches("^/api/members/wishlists/?$")) {
            return true;
        }

        // URI에서 wishlistId 추출
        String[] segments = uri.split("/");
        Long wishlistId;
        try {
            wishlistId = Long.parseLong(segments[WISHLIST_ID_INDEX]);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "유효하지 않은 위시리스트 ID입니다.");
            return false;
        }

        // 위시리스트 생성자 검증
        Long memberId = wishlistRepository.findMemberIdByWishlistId(wishlistId).orElse(null);

        if (memberId == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "위시리스트를 찾을 수 없습니다.");
            return false;
        }

        if (!memberId.equals(requestMemberId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "위시리스트에 대한 접근 권한이 없습니다.");
            return false;
        }

        // 위시리스트-숙소 생성, 조회 통과 - 없어도 되지만 명시적으로 표시
        if ((method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("GET"))
            && uri.matches("^/api/members/wishlists/\\d+/accommodations/?$")) {
            return true;
        }

        if ((method.equalsIgnoreCase("PATCH") || method.equalsIgnoreCase("DELETE"))
            && uri.matches("^/api/members/wishlists/\\d+/accommodations/\\d+$")) {
            // 위시리스트 숙소 메모 수정, 삭제인 경우 위시리스트 내부 항목인지 검증 절차
            Long wishlistAccommodationId;

            try {
                wishlistAccommodationId = Long.parseLong(segments[WISHLIST_ACCOMMODATION_ID_INDEX]);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "유효하지 않은 위시리스트-숙소 ID입니다.");
                return false;
            }

            Long foundWishlistId = wishlistAccommodationRepository.findWishlistIdByWishlistAccommodationId(
                wishlistAccommodationId).orElse(null);

            if (foundWishlistId == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "위시리스트-숙소를 찾을 수 없습니다.");
                return false;
            }

            if (!foundWishlistId.equals(wishlistId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "해당 위시리스트에 속하지 않은 숙소입니다.");
                return false;
            }
        }
        return true;
    }
}
