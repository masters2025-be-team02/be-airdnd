package kr.kro.airbob.domain.reservation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservationRequestDto {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateReservationDto {
        private String message;
        @NotNull
        private LocalDate checkInDate;
        @NotNull
        private LocalDate checkOutDate;
    }

}
