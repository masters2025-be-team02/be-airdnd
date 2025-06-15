package kr.kro.airbob.domain.accommodation.repository.querydsl;

import java.util.List;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest.AccommodationSearchConditionDto;
import kr.kro.airbob.domain.accommodation.dto.AccommodationResponse.AccommodationSearchResponseDto;
import org.springframework.data.domain.Pageable;

public interface AccommodationRepositoryCustom {
    List<AccommodationSearchResponseDto> searchByFilter(AccommodationSearchConditionDto condition, Pageable pageable);
}
