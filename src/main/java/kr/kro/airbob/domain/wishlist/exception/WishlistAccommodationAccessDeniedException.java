package kr.kro.airbob.domain.wishlist.exception;

public class WishlistAccommodationAccessDeniedException extends RuntimeException{

	public static final String ERROR_MESSAGE = "위시리스트 항목에 대한 접근 권한이 없습니다.";


	public WishlistAccommodationAccessDeniedException() {
		super(ERROR_MESSAGE);
	}

	public WishlistAccommodationAccessDeniedException(String message) {
		super(message);
	}

	public WishlistAccommodationAccessDeniedException(String message, Throwable cause) {
		super(message, cause);
	}
}
