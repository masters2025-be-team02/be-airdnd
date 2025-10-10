package kr.kro.airbob.cursor.exception;

public class CursorEncodingException extends RuntimeException {

	public static final String ERROR_MESSAGE = "커서 인코딩 중 예외가 발생하였습니다.";

	public CursorEncodingException(String message) {
		super(message);
	}

	public CursorEncodingException() {
		super(ERROR_MESSAGE);
	}

	public CursorEncodingException(String message, Throwable cause) {
		super(message, cause);
	}
}
