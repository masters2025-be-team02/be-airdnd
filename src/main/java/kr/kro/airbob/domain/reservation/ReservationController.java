package kr.kro.airbob.domain.reservation;

import kr.kro.airbob.domain.reservation.dto.ReservationRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accommodations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/{accommodationId}")
    public ResponseEntity<Map<String,Long>> createReservation(@PathVariable Long accommodationId, @RequestBody ReservationRequestDto.CreateReservationDto createReservationDto) {
        //todo 커스텀 에러 생성
        Long reservationId = reservationService.createReservation(accommodationId, createReservationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", reservationId));
    }

}
