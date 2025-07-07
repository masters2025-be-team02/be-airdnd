package kr.kro.airbob.domain.reservation.repository;

import kr.kro.airbob.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	boolean existsByAccommodationIdAndGuestId(Long accommodationId, Long memberId);

    @Query("SELECT r.guest.id FROM Reservation r WHERE r.id = :reservationId")
    Optional<Long> findMemberIdById(@Param("reservationId") Long reservationId);

}
