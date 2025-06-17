package kr.kro.airbob.domain.reservation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

public class ReservationRequestDto {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CreateReservationDto {
        private String message;
        @NotNull
        private LocalDate checkInDate;
        @NotNull
        private LocalDate checkOutDate;
    }

}
