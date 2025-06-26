package kr.kro.airbob.domain.reservation.repository;

import kr.kro.airbob.domain.reservation.entity.ReservedDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservedDateRepository extends JpaRepository<ReservedDate, Long> {

    @Query("SELECT r FROM ReservedDate r " +
            "WHERE r.accommodation.id = :accommodationId " +
            "AND r.reservedAt >= :checkIn AND r.reservedAt < :checkOut")
    List<ReservedDate> findReservedDates(
            @Param("accommodationId") Long accommodationId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

}
