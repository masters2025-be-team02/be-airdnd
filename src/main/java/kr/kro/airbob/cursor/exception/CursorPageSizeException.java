package kr.kro.airbob.cursor.exception;

public class CursorPageSizeException extends RuntimeException{

	private static final String ERROR_MESSAGE = "커서 페이지 크기는 1 이상이여야 합니다.";

	public CursorPageSizeException(String message) {
		super(message);
	}
	public CursorPageSizeException() {
		super(ERROR_MESSAGE);
	}

	public CursorPageSizeException(String message, Throwable cause) {
		super(message, cause);
	}
}
