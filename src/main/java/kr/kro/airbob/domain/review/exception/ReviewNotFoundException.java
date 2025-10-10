package kr.kro.airbob.domain.review.exception;

public class ReviewNotFoundException extends RuntimeException{

	public static final String ERROR_MESSAGE = "존재하지 않는 후기입니다.";

	public ReviewNotFoundException() {
		super(ERROR_MESSAGE);
	}
}
