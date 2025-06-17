package kr.kro.airbob.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
public class SessionAuthFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, Object> redisTemplate;

    public SessionAuthFilter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("doFilterInternal");
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (path.equals("/api/accommodations") && method.equals("GET")) {
            filterChain.doFilter(request, response); // 필터 건너뜀
            return;
        }


        Cookie[] cookies = request.getCookies();
        String sessionId = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("SESSION_ID".equals(cookie.getName())) {
                    sessionId = cookie.getValue();
                    break;
                }
            }
        }

        // 세션 ID가 없거나 레디스에 없으면 401 Unauthorized 반환
        if (sessionId == null || !Boolean.TRUE.equals(redisTemplate.hasKey("SESSION:" + sessionId))) {
            log.warn("[SessionAuthFilter] 인증 실패 - 세션 없음 또는 무효: {}", sessionId);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"인증이 필요합니다.\"}");
            return;
        }

        Long memberId = (Long) redisTemplate.opsForValue().get("SESSION:" + sessionId);
        request.setAttribute("memberId", memberId);

        filterChain.doFilter(request, response);
    }
}
