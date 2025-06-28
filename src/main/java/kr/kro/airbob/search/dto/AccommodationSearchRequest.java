package kr.kro.airbob.search.dto;

import java.time.LocalDate;
import java.util.List;

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
		private Integer minPrice;
		private Integer maxPrice;

		// 날짜
		private LocalDate checkIn;
		private LocalDate checkOut;

		// 인원 수
		private Integer adultOccupancy;
		private Integer childOccupancy;
		private Integer infantOccupancy;
		private Integer petOccupancy;

		// 편의시설
		private List<String> amenityTypes;

		// 숙소 타입
		private List<String> accommodationTypes;

		// 총 인원 수 계산
		public int getTotalGuests() {
			return (adultOccupancy != null ? adultOccupancy : 0) +
				(childOccupancy != null ? childOccupancy : 0) +
				(infantOccupancy != null ? infantOccupancy : 0) +
				(petOccupancy != null ? petOccupancy : 0);
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
