package kr.kro.airbob.domain.event;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

import kr.kro.airbob.domain.event.common.ApplyResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


@SpringBootTest
class EventServiceTest {

    @Autowired
    private EventService eventService;

    @MockitoBean
    private StringRedisTemplate redisTemplate;

    @Test
    void circuitBreaker_opens_after_consecutive_failures() {
        // given
        given(redisTemplate.execute(any(), anyList(), any(), any()))
                .willThrow(new RedisConnectionFailureException("Redis down"));

        // when
        ApplyResult result = eventService.applyToEvent(1L, 999L, 5);

        // then: fallback이 호출되어 ERROR 반환
        assertThat(result).isEqualTo(ApplyResult.ERROR);
    }
}
