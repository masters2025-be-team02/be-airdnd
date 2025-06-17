package kr.kro.airbob.domain.reservation.exception;

public class AlreadyReservedException extends RuntimeException {

    public static final String ERROR_MESSAGE = "이미 예약된 날짜입니다.";

    public AlreadyReservedException() {
        super(ERROR_MESSAGE);
    }
}
