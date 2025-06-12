package kr.kro.airbob.domain.accommodation.repository;

import java.util.List;
import kr.kro.airbob.domain.accommodation.entity.AccommodationAmenity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccommodationAmenityRepository extends JpaRepository<AccommodationAmenity, Long> {
    void deleteAllByAccommodationId(Long accommodationId);

    List<AccommodationAmenity> findAllByAccommodationId(Long accommodationId);

    void deleteByAccommodationId(Long accommodationId);

    boolean existsByAccommodationId(Long accommodationId);
}
