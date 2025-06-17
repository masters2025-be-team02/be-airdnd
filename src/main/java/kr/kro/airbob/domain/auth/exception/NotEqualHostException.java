package kr.kro.airbob.domain.auth.exception;

public class NotEqualHostException extends RuntimeException{

	public static final String ERROR_MESSAGE = "호스트와 다른 사용자입니다.";

	public NotEqualHostException() {
		super(ERROR_MESSAGE);
	}
}
