package kr.kro.airbob.domain.accommodation.repository;

import java.util.Optional;
import kr.kro.airbob.domain.accommodation.entity.Accommodation;
import kr.kro.airbob.domain.accommodation.entity.Address;
import kr.kro.airbob.domain.accommodation.repository.querydsl.AccommodationRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long>, AccommodationRepositoryCustom {
    Optional<Address> findAddressById(Long accommodationId);
}
