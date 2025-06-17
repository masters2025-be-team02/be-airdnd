package kr.kro.airbob.domain.reservation;

import kr.kro.airbob.domain.reservation.dto.ReservationRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accommodations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/{accommodationId}")
    public ResponseEntity<Long> createReservation(@PathVariable Long accommodationId, @RequestBody ReservationRequestDto.CreateReservationDto createReservationDto) {
        //todo 커스텀 에러 생성
        Long reservationId = reservationService.createReservation(accommodationId, createReservationDto)
                .orElseThrow(() -> new IllegalArgumentException("해당 날짜는 예약할 수 없습니다."));
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationId);
    }

}
