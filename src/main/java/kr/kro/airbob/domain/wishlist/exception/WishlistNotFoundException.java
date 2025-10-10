package kr.kro.airbob.domain.wishlist.exception;

public class WishlistNotFoundException extends RuntimeException {

	public static final String ERROR_MESSAGE = "존재하지 않는 위시리스트입니다.";

	public WishlistNotFoundException() {
		super(ERROR_MESSAGE);
	}
}
