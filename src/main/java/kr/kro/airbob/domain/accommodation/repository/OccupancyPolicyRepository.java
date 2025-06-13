package kr.kro.airbob.domain.accommodation.repository;

import java.util.Collection;
import java.util.List;
import kr.kro.airbob.domain.accommodation.common.AmenityType;
import kr.kro.airbob.domain.accommodation.entity.Amenity;
import kr.kro.airbob.domain.accommodation.entity.OccupancyPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OccupancyPolicyRepository extends JpaRepository<OccupancyPolicy, Long> {
}
