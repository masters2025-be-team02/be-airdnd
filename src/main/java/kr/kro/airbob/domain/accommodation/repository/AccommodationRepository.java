package kr.kro.airbob.domain.accommodation.repository;

import java.util.Optional;
import kr.kro.airbob.domain.accommodation.entity.Accommodation;
import kr.kro.airbob.domain.accommodation.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
    Optional<Address> findAddressById(Long accommodationId);
}
