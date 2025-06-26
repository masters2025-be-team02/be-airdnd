package kr.kro.airbob.domain.reservation.repository;

import kr.kro.airbob.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	boolean existsByAccommodationIdAndGuestId(Long accommodationId, Long memberId);

    Optional<Long> findMemberIdById(Long reservationId);
}
