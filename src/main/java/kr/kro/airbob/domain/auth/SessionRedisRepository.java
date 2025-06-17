package kr.kro.airbob.domain.auth;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SessionRedisRepository {
    private static final Duration TTL = Duration.ofHours(1);
    private static final String SESSION = "SESSION:";
    private final RedisTemplate<String, Object> redisTemplate;

    public void saveSession(String sessionId, Long memberId) {
        redisTemplate.opsForValue().set(SESSION + sessionId, memberId, TTL);
    }

    public Optional<Long> getMemberIdBySession(String sessionId) {
        Object value = redisTemplate.opsForValue().get(SESSION + sessionId);
        return Optional.of((Long) value);
    }

    public void deleteSession(String sessionId) {
        redisTemplate.delete(SESSION + sessionId);
    }
}
