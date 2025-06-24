package kr.kro.airbob.domain.event;

import java.time.LocalDateTime;
import java.util.Arrays;
import kr.kro.airbob.domain.event.common.ApplyResult;
import kr.kro.airbob.domain.event.entity.Event;
import kr.kro.airbob.domain.event.entity.EventParticipant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventService {

    private final StringRedisTemplate redisTemplate;
    private final EventParticipantRepository eventParticipantRepository;
    private final EventRepository eventRepository;

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

        String result = redisTemplate.execute(script,
                Arrays.asList("event:" + eventId + ":set", "event:" + eventId + ":queue"),
                String.valueOf(memberId), String.valueOf(maxParticipants));

        return ApplyResult.valueOf(result.toUpperCase());
    }

    @Async
    public void consumeQueue(Long eventId) {
        String queueKey = "event:" + eventId + ":queue";

        while (true) {
            String memberId = redisTemplate.opsForList().leftPop(queueKey);
            if (memberId == null) break;

            try {
                saveToDatabase(eventId, Long.valueOf(memberId));
            } catch (Exception e) {
                log.error("DB 저장 실패: memberId={}, error={}", memberId, e.getMessage());
            }
        }
    }

    @Transactional
    protected void saveToDatabase(Long eventId, Long memberId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트가 존재하지 않습니다."));

        if (event.getEndAt().isBefore(LocalDateTime.now())){
            throw new IllegalStateException("이벤트 마감됨");
        }

        long count = eventParticipantRepository.countByEventId(eventId);
        if (count >= event.getMaxParticipants()) {
            throw new IllegalStateException("이벤트 마감됨");
        }

        EventParticipant eventParticipant = EventParticipant.builder()
                .event(event)
                .memberId(memberId)
                .build();

        eventParticipantRepository.save(eventParticipant);
    }

    public int getEventMaxParticipants(Long eventId) {
        return eventRepository.findMaxParticipantsById(eventId);
    }
}
