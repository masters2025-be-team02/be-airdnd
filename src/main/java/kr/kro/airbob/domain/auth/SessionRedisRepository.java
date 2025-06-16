package kr.kro.airbob.domain.auth;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SessionRedisRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final Duration TTL = Duration.ofHours(1);

    public void saveSession(String sessionId, Long memberId) {
        redisTemplate.opsForValue().set("SESSION:" + sessionId, memberId, TTL);
    }

    public Optional<Long> getMemberIdBySession(String sessionId) {
        Object value = redisTemplate.opsForValue().get("SESSION:" + sessionId);
        return Optional.ofNullable((Long) value);
    }

    public void deleteSession(String sessionId) {
        redisTemplate.delete("SESSION:" + sessionId);
    }
}
