package kr.kro.airbob.cursor.exception;

public class CursorException extends RuntimeException {

	public CursorException(String message) {
		super(message);
	}

	public CursorException(String message, Throwable cause) {
		super(message, cause);
	}
}
