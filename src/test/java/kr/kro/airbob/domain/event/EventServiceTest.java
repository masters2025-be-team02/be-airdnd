package kr.kro.airbob.domain.event;

import java.time.LocalDateTime;
import java.util.Optional;
import kr.kro.airbob.domain.event.entity.Event;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventParticipantRepository eventParticipantRepository;

    @Test
    @DisplayName("이벤트가 존재하지 않으면 예외가 발생한다")
    void shouldThrowExceptionWhenEventNotFound() {
        // given
        Long eventId = 1L;
        Long memberId = 42L;

        Mockito.when(eventRepository.findById(eventId))
                .thenReturn(Optional.empty());

        // when & then
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            eventService.saveToDatabase(eventId, memberId);
        });
    }

    @Test
    @DisplayName("참여자가 최대인원 초과시 예외가 발생한다")
    void shouldThrowExceptionWhenParticipantsExceedLimit() {
        // given
        Long eventId = 1L;
        Long memberId = 42L;
        Event event = Event.builder()
                .id(eventId)
                .name("이벤트")
                .endAt(LocalDateTime.now().plusDays(1))
                .maxParticipants(100)
                .build();

        Mockito.when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));
        Mockito.when(eventParticipantRepository.countByEventId(eventId))
                .thenReturn(100L); // 정원 도달

        // when & then
        Assertions.assertThrows(IllegalStateException.class, () -> {
            eventService.saveToDatabase(eventId, memberId);
        });
    }
}
