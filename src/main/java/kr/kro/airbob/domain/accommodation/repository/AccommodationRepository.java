package kr.kro.airbob.domain.accommodation.repository;

import kr.kro.airbob.domain.accommodation.entity.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
}
