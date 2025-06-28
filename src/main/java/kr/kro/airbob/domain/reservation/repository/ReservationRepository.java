package kr.kro.airbob.domain.reservation.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.kro.airbob.domain.reservation.common.ReservationStatus;
import kr.kro.airbob.domain.reservation.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	boolean existsByAccommodationIdAndGuestId(Long accommodationId, Long memberId);

	@Query("""
        SELECT r FROM Reservation r 
        WHERE r.accommodation.id = :accommodationId 
        AND r.status = :status 
        AND r.checkOut >= :today
        """)
	List<Reservation> findFutureReservationsByAccommodationIdAndStatus(
		@Param("accommodationId") Long accommodationId,
		@Param("status") ReservationStatus status,
		@Param("today") LocalDateTime today
	);

    Optional<Long> findMemberIdById(Long reservationId);
}
