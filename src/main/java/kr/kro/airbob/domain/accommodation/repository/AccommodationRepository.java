package kr.kro.airbob.domain.accommodation.repository;

import java.util.Optional;
import java.util.OptionalLong;
import kr.kro.airbob.domain.accommodation.entity.Accommodation;
import kr.kro.airbob.domain.accommodation.entity.Address;
import kr.kro.airbob.domain.accommodation.repository.querydsl.AccommodationRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long>, AccommodationRepositoryCustom {
    Optional<Address> findAddressById(Long accommodationId);

    @Query("select a.member.id from Accommodation a where a.id = :id")
    Optional<Long> findHostIdByAccommodationId(Long id);
}
