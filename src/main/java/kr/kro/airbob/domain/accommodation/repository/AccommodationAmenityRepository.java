package kr.kro.airbob.domain.accommodation.repository;

import kr.kro.airbob.domain.accommodation.entity.Accommodation;
import kr.kro.airbob.domain.accommodation.entity.AccommodationAmenity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccommodationAmenityRepository extends JpaRepository<AccommodationAmenity, Long> {
}
