package kr.kro.airbob.domain.auth.exception;

public class InvalidPasswordException extends RuntimeException{

	public static final String ERROR_MESSAGE = "비밀번호가 일치하지 않습니다.";

	public InvalidPasswordException() {
		super(ERROR_MESSAGE);
	}
}
