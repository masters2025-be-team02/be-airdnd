package kr.kro.airbob.domain.event;

import java.time.Duration;
import java.util.Arrays;
import kr.kro.airbob.domain.event.common.ApplyResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventService {

    private final CacheManager cacheManager;
    private final StringRedisTemplate redisTemplate;
    private final EventRepository eventRepository;
    private final EventSaver eventSaver;

    private final String APPLY_EVENT_SCRIPT = """
                local isAlreadyApplied = redis.call("SISMEMBER", KEYS[1], ARGV[1])
                if isAlreadyApplied == 1 then
                    return "duplicate"
                end
                                
                local currentQueueSize = redis.call("LLEN", KEYS[2])
                local maxQueueSize = tonumber(ARGV[2])
                if currentQueueSize >= maxQueueSize then
                    return "full"
                end
                                
                redis.call("SADD", KEYS[1], ARGV[1])
                redis.call("RPUSH", KEYS[2], ARGV[1])
                return "success"
                """;

    @Transactional
    public ApplyResult applyToEvent(Long eventId, Long memberId, int maxParticipants) {

        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setScriptText(APPLY_EVENT_SCRIPT);
        script.setResultType(String.class);

        String keySet = "event:" + eventId + ":set";
        String keyQueue = "event:" + eventId + ":queue";

        String result = redisTemplate.execute(script,
                Arrays.asList(keySet, keyQueue),
                String.valueOf(memberId), String.valueOf(maxParticipants));

        Long ttlSeconds = 300L;
        redisTemplate.expire(keySet, Duration.ofSeconds(ttlSeconds));
        redisTemplate.expire(keyQueue, Duration.ofSeconds(ttlSeconds));

        return ApplyResult.valueOf(result.toUpperCase());
    }

    @Async
    public void consumeQueue(Long eventId) {
        String queueKey = "event:" + eventId + ":queue";

        while (true) {
            String memberId = redisTemplate.opsForList().leftPop(queueKey);
            if (memberId == null) break;

            try {
                eventSaver.saveToDatabase(eventId, Long.valueOf(memberId));
            } catch (Exception e) {
                log.error("DB 저장 실패: memberId={}, error={}", memberId, e.getMessage());
            }
        }
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
