package kr.kro.airbob.domain.accommodation.dto;

import java.util.List;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest.AmenityInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class AccommodationResponse {
    @Getter
    @Builder
    @AllArgsConstructor
    public static class AccommodationSearchResponseDto {
        private String name;
        private String thumbnailUrl;
        private Integer pricePerNight;
        private Integer maxOccupancy;
        private List<AmenityInfo> amenityInfos;
        private Double averageRating;
        private Integer reviewCount;
    }
}
