package kr.kro.airbob.domain.reservation.entity;

import jakarta.persistence.*;
import kr.kro.airbob.common.domain.BaseEntity;
import kr.kro.airbob.domain.accommodation.entity.Accommodation;
import kr.kro.airbob.domain.reservation.common.ReservationStatus;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@Table(name = "reserved_dates")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservedDate extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate reservedAt;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @ManyToOne
    @JoinColumn(name = "accommodation_id")
    private Accommodation accommodation;

    public void completeReservation() {
        this.status = ReservationStatus.COMPLETED;
    }
}
