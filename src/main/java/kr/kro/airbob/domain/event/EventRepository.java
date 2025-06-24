package kr.kro.airbob.domain.event;

import kr.kro.airbob.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("SELECT e.maxParticipants FROM Event e WHERE e.id = :eventId")
    Long findMaxParticipantsById(@Param("eventId") Long eventId);
}
