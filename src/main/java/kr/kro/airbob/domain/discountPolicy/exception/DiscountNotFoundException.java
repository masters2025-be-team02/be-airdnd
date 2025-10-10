package kr.kro.airbob.domain.discountPolicy.exception;

public class DiscountNotFoundException extends RuntimeException {

    private static final String ERROR_MESSAGE = "존재하지 않는 할인정책입니다.";

    public DiscountNotFoundException() {
        super(ERROR_MESSAGE);
    }
}
