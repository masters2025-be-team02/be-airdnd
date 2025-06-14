package kr.kro.airbob.domain.accommodation.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class AccommodationRequest {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CreateAccommodationDto{
        @NotNull
        private String name;
        @NotNull
        private String description;
        @NotNull
        private int basePrice;
        @NotNull
        private Long hostId;
        @NotNull
        private AddressInfo addressInfo;
        private List<AmenityInfo> amenityInfos;
        private OccupancyPolicyInfo occupancyPolicyInfo;
        private String thumbnail_url;
        private String type;

    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AddressInfo{
        private Integer postalCode;
        private String city;
        private String country;
        private String detail;
        private String district;
        private String street;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AmenityInfo {
        private String name;
        private Integer count;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OccupancyPolicyInfo {
        private Integer maxOccupancy;
        private Integer adultOccupancy;
        private Integer childOccupancy;
        private Integer infantOccupancy;
        private Integer petOccupancy;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UpdateAccommodationDto {
        private String name;
        private String description;
        private Integer basePrice;
        private AddressInfo addressInfo;
        private OccupancyPolicyInfo occupancyPolicyInfo;
        private List<AmenityInfo> amenityInfos;
        private String type;
    }
}
