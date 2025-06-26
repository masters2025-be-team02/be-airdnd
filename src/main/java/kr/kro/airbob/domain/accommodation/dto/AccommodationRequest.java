package kr.kro.airbob.domain.accommodation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class AccommodationRequest {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CreateAccommodationDto{
        @NotBlank
        @Size(min = 1, max = 50, message = "이름은 1 ~ 50자 이여야 합니다!")
        private String name;
        @NotBlank
        @Size(min = 1, max = 500, message = "설명은 1 ~ 500자 이여야 합니다!")
        private String description;
        @NotNull
        private int basePrice;
        @NotNull
        private Long hostId;
        @NotNull
        private AddressInfo addressInfo;
        private List<AmenityInfo> amenityInfos;
        @NotNull
        private OccupancyPolicyInfo occupancyPolicyInfo;
        private String thumbnailUrl;
        @NotBlank
        private String type;

    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AddressInfo{
        @NotNull
        private String postalCode;
        @NotBlank
        private String city;
        @NotBlank
        private String country;
        @NotBlank
        private String detail;
        @NotBlank
        private String district;
        @NotBlank
        private String street;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AmenityInfo {
        @NotBlank
        private String name;
        @NotNull
        private Integer count;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OccupancyPolicyInfo {
        @NotNull
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

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AccommodationSearchConditionDto {
        private String city;
        private Integer minPrice;
        private Integer maxPrice;
        private LocalDate checkIn;
        private LocalDate checkOut;
        private Integer guestCount;
        private List<String> amenityTypes;
        private List<String> accommodationTypes;
    }
}
