package kr.kro.airbob.domain.event.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import kr.kro.airbob.common.domain.BaseEntity;
import kr.kro.airbob.domain.event.common.ParticipationStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventParticipant extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Event event;

    private LocalDateTime appliedAt;

    @Enumerated(EnumType.STRING)
    private ParticipationStatus status;
}
