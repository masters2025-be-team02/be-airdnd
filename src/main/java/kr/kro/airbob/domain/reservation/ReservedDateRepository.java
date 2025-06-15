package kr.kro.airbob.domain.reservation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservedDateRepository extends JpaRepository<ReservedDate, Long> {
}
