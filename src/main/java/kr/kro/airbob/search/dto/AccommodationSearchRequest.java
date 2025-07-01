package kr.kro.airbob.search.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccommodationSearchRequest {

	@Getter
	public static class AccommodationSearchRequestDto {

		// 목적지
		private String destination;

		// 가격 필터
		@Min(value = 0, message = "최소 가격은 0원 이상이어야 합니다")
		private Integer minPrice;
		@Min(value = 0, message = "최대 가격은 0원 이상이어야 합니다")
		private Integer maxPrice;

		// 날짜
		private LocalDate checkIn;
		private LocalDate checkOut;

		// 인원 수
		@PositiveOrZero
		private Integer adultOccupancy;
		@PositiveOrZero
		private Integer childOccupancy;
		@PositiveOrZero
		private Integer infantOccupancy;
		@PositiveOrZero
		private Integer petOccupancy;

		// 편의시설
		private List<String> amenityTypes;

		// 숙소 타입
		private List<String> accommodationTypes;

		// 총 인원 수 계산 (유아, 펫 제외)
		public int getTotalGuests() {
			return (adultOccupancy != null ? adultOccupancy : 0) +
				(childOccupancy != null ? childOccupancy : 0);
		}

		public boolean hasPet() {
			return (petOccupancy != null) && petOccupancy > 0;
		}

		public boolean isValidOccupancy() {
			int adults = adultOccupancy != null ? adultOccupancy : 0;
			return adults >= 1;
		}

		public boolean isValidPriceRange() {
			if (minPrice != null && maxPrice != null) {
				return minPrice <= maxPrice;
			}
			return true;
		}

		public void setDefaultOccupancy() {
			if (adultOccupancy == null || adultOccupancy < 1) {
				this.adultOccupancy = 1;
			}
			if (childOccupancy == null) {
				this.childOccupancy = 0;
			}
			if (infantOccupancy == null) {
				this.infantOccupancy = 0;
			}
			if (petOccupancy == null) {
				this.petOccupancy = 0;
			}
		}
	}

	@Getter
	public static class MapBoundsDto {
		// 지도 드래그 영역
		private Double topLeftLat;
		private Double topLeftLng;
		private Double bottomRightLat;
		private Double bottomRightLng;

		public boolean isValid() {
			return topLeftLat != null && topLeftLng != null &&
				bottomRightLat != null && bottomRightLng != null;
		}
	}
}
