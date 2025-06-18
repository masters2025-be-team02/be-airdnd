package kr.kro.airbob.domain.reservation.repository;

import kr.kro.airbob.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
