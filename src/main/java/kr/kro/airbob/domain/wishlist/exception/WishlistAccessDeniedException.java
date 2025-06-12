package kr.kro.airbob.domain.wishlist.exception;

public class WishlistAccessDeniedException extends RuntimeException{

	public static final String ERROR_MESSAGE = "위시리스트에 대한 접근 권한이 없습니다.";

	public WishlistAccessDeniedException() {
		super(ERROR_MESSAGE);
	}
}
