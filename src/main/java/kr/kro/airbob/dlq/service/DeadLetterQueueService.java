package kr.kro.airbob.event.service;

import static kr.kro.airbob.event.entity.FailedIndexingEvent.EventStatus.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.kro.airbob.event.entity.FailedIndexingEvent;
import kr.kro.airbob.event.repository.FailedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeadLetterQueueService {

	private final FailedEventRepository failedEventRepository;
	private final ObjectMapper objectMapper;

	private static final int MAX_RETRY_COUNT = 5;

	@Transactional
	public void savedFailedEvent(String eventType, Object eventData, Exception exception) {
		String errorMessage = extractErrorMessage(exception);

		try {
			String serializedEventData = objectMapper.writeValueAsString(eventData);
			FailedIndexingEvent failedEvent =
				FailedIndexingEvent.create(eventType, serializedEventData, errorMessage);

			failedEventRepository.save(failedEvent);

			log.warn("이벤트 처리 실패로 DLQ에 저장: eventType={}, error={}",
				eventType, errorMessage);
		} catch (Exception e) {
			log.error("이벤트 직렬화 실패: eventType={}, error={}", eventType, e.getMessage(), e);
			throw new IllegalArgumentException("이벤트 데이터 직렬화 실패", e);
		}
	}

	@Scheduled(fixedDelay = 300000) // 5분
	@Transactional
	public void retryFailedEvents() {
		LocalDateTime now = LocalDateTime.now();
		List<FailedIndexingEvent> eventsToRetry =
			failedEventRepository.findEventsReadyForRetry(now, MAX_RETRY_COUNT);

		if (eventsToRetry.isEmpty()) {
			return;
		}

		for (FailedIndexingEvent failedEvent : eventsToRetry) {
			try{
				failedEvent.updateStatus(RETRYING);
				failedEventRepository.save(failedEvent);


			}
		}
	}

	private boolean reprocessEvent(FailedIndexingEvent failedEvent) {
		try{
			String eventType = failedEvent.getEventType();
			String eventData = failedEvent.getEventData();

			switch (eventType)
		}
	}

	private String extractErrorMessage(Exception e) {
		if(e == null) return "Unknown error";

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
}
