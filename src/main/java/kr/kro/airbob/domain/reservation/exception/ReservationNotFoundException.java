package kr.kro.airbob.domain.reservation.exception;

public class ReservationNotFoundException extends RuntimeException {

    public static final String ERROR_MESSAGE = "찾을 수 없는 예약입니다.";

    public ReservationNotFoundException() {
        super(ERROR_MESSAGE);
    }
}
