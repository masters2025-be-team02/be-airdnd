package kr.kro.airbob.domain.event;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.Duration;
import java.util.Arrays;
import kr.kro.airbob.domain.event.common.ApplyResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventService {

    private static final Duration FULL_TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;
    private final EventRepository eventRepository;

    private final String APPLY_EVENT_SCRIPT = """
        local memberId = ARGV[1]
        local maxQueueSize = tonumber(ARGV[2])
        
        local zsetKey = KEYS[1]
        local seqKey = KEYS[2]
        
        -- 순번 증가 (score로 사용)
        local score = redis.call('INCR', seqKey)
        
        -- 추가
        local added = redis.call('ZADD', zsetKey, 'NX', score, memberId)
        if added == 0 then
            return "duplicate"
        end
        
        -- 순위 확인
        local rank = redis.call('ZRANK', zsetKey, memberId)
        if rank >= maxQueueSize then
            return "full"
        end
        
        return "success"
    """;
    @Transactional
    @CircuitBreaker(name = "redisEventQueue", fallbackMethod = "fallback")
    public ApplyResult applyToEvent(Long eventId, Long memberId, int maxParticipants) {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setScriptText(APPLY_EVENT_SCRIPT);
        script.setResultType(String.class);

        String zsetKey = "event:" + eventId + ":zset";
        String seqKey = "event:" + eventId + ":seq";

        String result = redisTemplate.execute(
                script,
                Arrays.asList(zsetKey, seqKey),
                String.valueOf(memberId),
                String.valueOf(maxParticipants)
        );


        long ttlSeconds = 300L;
        redisTemplate.expire(zsetKey, Duration.ofSeconds(ttlSeconds));

        return ApplyResult.valueOf(result.toUpperCase());
    }

    private ApplyResult fallback(Throwable t) {
        return ApplyResult.ERROR;
    }
    @Cacheable(value = "eventMaxParticipantsCache", key = "'event:' + #eventId + ':maxParticipants'")
    public int getEventMaxParticipants(Long eventId) {
        Long max = eventRepository.findMaxParticipantsById(eventId);
        return max.intValue();
    }

    public void publishEventQueueIsFull(Long eventId) {
        redisTemplate.convertAndSend("event:" + eventId + ":notifications", "이벤트 마감");
    }

    public boolean isEventFull(Long eventId) {
        String fullKey = "event:" + eventId + ":full";
        return Boolean.TRUE.equals(redisTemplate.hasKey(fullKey));
    }

    public boolean markEventFullIfAbsent(Long eventId) {
        String fullKey = "event:" + eventId + ":full";
        Boolean success = redisTemplate.opsForValue().setIfAbsent(fullKey, "true", FULL_TTL);
        return Boolean.TRUE.equals(success);
    }

}
