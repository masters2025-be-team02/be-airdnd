package kr.kro.airbob.domain.event;

import kr.kro.airbob.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
