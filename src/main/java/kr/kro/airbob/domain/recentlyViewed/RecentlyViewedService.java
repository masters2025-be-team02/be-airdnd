package kr.kro.airbob.domain.recentlyViewed;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import kr.kro.airbob.domain.accommodation.repository.AccommodationAmenityRepository;
import kr.kro.airbob.domain.accommodation.repository.AccommodationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecentlyViewedService {

	private final RedisTemplate<String, String> redisTemplate;
	private final AccommodationRepository accommodationRepository;
	private final AccommodationAmenityRepository accommodationAmenityRepository;

	private static final String RECENTLY_VIEWED_KEY_PREFIX = "recently_viewed:";
	private static final int MAX_COUNT = 100;
	private static final long TTL_DAYS = 7;

	public void addRecentlyViewed(Long memberId, Long accommodationId) {
		String key = RECENTLY_VIEWED_KEY_PREFIX + memberId;
		long timestamp = System.currentTimeMillis();

		redisTemplate.opsForZSet().add(key, accommodationId.toString(), timestamp);

		Long currentSize = redisTemplate.opsForZSet().size(key);
		if (currentSize != null && currentSize > MAX_COUNT) {
			redisTemplate.opsForZSet().removeRange(key, 0, currentSize - MAX_COUNT - 1);
		}
		redisTemplate.expire(key, Duration.ofDays(TTL_DAYS));
	}
}
