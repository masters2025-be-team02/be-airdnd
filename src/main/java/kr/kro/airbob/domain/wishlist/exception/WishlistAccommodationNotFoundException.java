package kr.kro.airbob.domain.wishlist.exception;

public class WishlistAccommodationNotFoundException extends RuntimeException{

	public static final String ERROR_MESSAGE = "존재하지 않는 위시리스트 항목입니다.";

	public WishlistAccommodationNotFoundException() {
		super(ERROR_MESSAGE);
	}
	public WishlistAccommodationNotFoundException(String message) {
		super(message);
	}

	public WishlistAccommodationNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
