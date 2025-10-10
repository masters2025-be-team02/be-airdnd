package kr.kro.airbob.domain.accommodation.exception;

public class AccommodationNotFoundException extends RuntimeException {

    public static final String ERROR_MESSAGE = "존재하지 않는 숙소입니다.";

    public AccommodationNotFoundException() {
        super(ERROR_MESSAGE);
    }
}
