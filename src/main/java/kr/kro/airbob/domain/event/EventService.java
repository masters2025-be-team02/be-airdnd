package kr.kro.airbob.domain.event;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import kr.kro.airbob.domain.event.common.ApplyResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventService {

    private final CacheManager cacheManager;
    private final StringRedisTemplate redisTemplate;
    private final EventRepository eventRepository;

    private final String APPLY_EVENT_SCRIPT = """
            local memberId = ARGV[1]
                local maxQueueSize = tonumber(ARGV[2])
                local channel = ARGV[3]
                
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
                    redis.call('ZREM', zsetKey, memberId)
                    redis.call('PUBLISH', channel, "이벤트 마감")
                    return "full"
                end
                
                return "success"
        """;

    @PostConstruct
    public void preloadCache() {
        Long eventId = 1L; // 캐싱할 이벤트 ID
        String key = "event:" + eventId + ":maxParticipants";
        Cache cache = cacheManager.getCache("eventMaxParticipantsCache");

        if (cache != null && cache.get(key) == null) {
            Long max = eventRepository.findMaxParticipantsById(eventId);
            cache.put(key, max.intValue());
            log.info("✅ 캐시 프리로드 완료: key={}, value={}", key, max);
        }
    }

    @Transactional
    public ApplyResult applyToEvent(Long eventId, Long memberId, int maxParticipants) {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setScriptText(APPLY_EVENT_SCRIPT);
        script.setResultType(String.class);

        String zsetKey = "event:" + eventId + ":zset";
        String redisChannel = "event:" + eventId + ":notifications";
        String seqKey = "event:" + eventId + ":seq";

        String result = redisTemplate.execute(
                script,
                Arrays.asList(zsetKey, seqKey),
                String.valueOf(memberId),
                String.valueOf(maxParticipants),
                redisChannel
        );


        long ttlSeconds = 300L;
        redisTemplate.expire(zsetKey, Duration.ofSeconds(ttlSeconds));

        return ApplyResult.valueOf(result.toUpperCase());
    }

    public int getEventMaxParticipants(Long eventId) {
        Cache cache = cacheManager.getCache("eventMaxParticipantsCache");
        String key = "event:" + eventId + ":maxParticipants";

        Integer cachedValue = cache.get(key, Integer.class);
        if (cachedValue != null) {
            return cachedValue;
        }

        Long max = eventRepository.findMaxParticipantsById(eventId); // DB 조회
        cache.put(key, max.intValue());
        return max.intValue();
    }
}
