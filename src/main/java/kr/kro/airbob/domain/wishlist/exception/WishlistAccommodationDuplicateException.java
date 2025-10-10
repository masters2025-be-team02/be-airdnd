package kr.kro.airbob.domain.wishlist.exception;

public class WishlistAccommodationDuplicateException extends RuntimeException {

	public static final String ERROR_MESSAGE = "이미 위시리스트에 추가된 숙소입니다.";

	public WishlistAccommodationDuplicateException(String message) {
		super(message);
	}

	public WishlistAccommodationDuplicateException() {
		super(ERROR_MESSAGE);
	}

	public WishlistAccommodationDuplicateException(String message, Throwable cause) {
		super(message, cause);
	}
}
