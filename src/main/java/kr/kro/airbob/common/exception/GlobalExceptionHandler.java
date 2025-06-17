package kr.kro.airbob.common.exception;

import java.util.stream.Collectors;

import kr.kro.airbob.domain.accommodation.exception.AccommodationNotFoundException;
import kr.kro.airbob.domain.reservation.exception.AlreadyReservedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import kr.kro.airbob.cursor.exception.CursorEncodingException;
import kr.kro.airbob.cursor.exception.CursorPageSizeException;
import kr.kro.airbob.domain.member.exception.MemberNotFoundException;
import kr.kro.airbob.domain.wishlist.exception.WishlistAccessDeniedException;
import kr.kro.airbob.domain.wishlist.exception.WishlistNotFoundException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class) // @Valid에서 발생하는 에러
	public ResponseEntity<Void> handleValidationExceptions(MethodArgumentNotValidException e) {
		String errorMessage = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(FieldError::getDefaultMessage)
			.collect(Collectors.joining(", "));

		log.error("Bean Validation error(@Valid): {}", errorMessage);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	}

	@ExceptionHandler(MemberNotFoundException.class)
	public ResponseEntity<Void> handleMemberNotFoundException(MemberNotFoundException e) {
		log.error("MemberNotFoundException: {}", e.getMessage());
		return ResponseEntity
			.status(HttpStatus.NOT_FOUND)
			.build();
	}

	@ExceptionHandler(AccommodationNotFoundException.class)
	public ResponseEntity<Void> handleAccommodationNotFoundException(AccommodationNotFoundException e) {
		log.error("AccommodationNotFoundException: {}", e.getMessage());
		return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.build();
	}

	@ExceptionHandler(AlreadyReservedException.class)
	public ResponseEntity<Void> handleAlreadyReservedException(AlreadyReservedException e) {
		log.error("AlreadyReservedException: {}", e.getMessage());
		return ResponseEntity
				.status(HttpStatus.CONFLICT)
				.build();
	}

	@ExceptionHandler(WishlistNotFoundException.class)
	public ResponseEntity<Void> handleWishlistNotFoundException(WishlistNotFoundException e) {
		log.error("WishlistNotFoundException: {}", e.getMessage());
		return ResponseEntity
			.status(HttpStatus.NOT_FOUND)
			.build();
	}
	@ExceptionHandler(WishlistAccessDeniedException.class)
	public ResponseEntity<Void> handleWishlistAccessDeniedException(WishlistAccessDeniedException e) {
		log.warn("WishlistAccessDeniedException: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	}

	@ExceptionHandler(CursorEncodingException.class)
	public ResponseEntity<Void> handleCursorEncodingException(CursorEncodingException e) {
		log.error("Cursor encoding fail: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	}

	@ExceptionHandler(CursorPageSizeException.class)
	public ResponseEntity<Void> handleCursorPageSizeException(CursorPageSizeException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Void> handleExceptions(Exception e) {
		log.error("Unhandled exception", e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	}
}
