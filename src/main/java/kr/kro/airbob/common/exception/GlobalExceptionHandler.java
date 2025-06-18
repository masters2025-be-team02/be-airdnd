package kr.kro.airbob.common.exception;

import java.util.stream.Collectors;

import kr.kro.airbob.domain.accommodation.exception.AccommodationNotFoundException;
import kr.kro.airbob.domain.reservation.exception.AlreadyReservedException;
import kr.kro.airbob.domain.auth.exception.NotEqualHostException;
import kr.kro.airbob.domain.member.exception.DuplicatedEmailException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import kr.kro.airbob.cursor.exception.CursorEncodingException;
import kr.kro.airbob.cursor.exception.CursorPageSizeException;
import kr.kro.airbob.domain.accommodation.exception.AccommodationNotFoundException;
import kr.kro.airbob.domain.member.exception.MemberNotFoundException;
import kr.kro.airbob.domain.review.ReviewSortType;
import kr.kro.airbob.domain.wishlist.exception.WishlistAccessDeniedException;
import kr.kro.airbob.domain.wishlist.exception.WishlistAccommodationAccessDeniedException;
import kr.kro.airbob.domain.wishlist.exception.WishlistAccommodationNotFoundException;
import kr.kro.airbob.domain.wishlist.exception.WishlistNotFoundException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<?> handleEnumBindingError(MethodArgumentTypeMismatchException e) {
		if (e.getRequiredType() == ReviewSortType.class) {
			return ResponseEntity.badRequest()
				.body("지원하지 않는 정렬 방식입니다. [LATEST, HIGH_RATING, LOW_RATING] 중 하나를 사용하세요.");
		}
		return ResponseEntity.badRequest().body("잘못된 요청입니다.");
	}

	@ExceptionHandler(AccommodationNotFoundException.class)
	public ResponseEntity<Void> handleAccommodationNotFoundException(AccommodationNotFoundException e) {
		log.error("AccommodationNotFoundException: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.build();
	}

	@ExceptionHandler(MemberNotFoundException.class)
	public ResponseEntity<Void> handleMemberNotFoundException(MemberNotFoundException e) {
		log.error("MemberNotFoundException: {}", e.getMessage());
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

	@ExceptionHandler(WishlistAccommodationAccessDeniedException.class)
	public ResponseEntity<Void> handleWishlistAccommodationAccessDeniedException(
		WishlistAccommodationAccessDeniedException e) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	}

	@ExceptionHandler(WishlistAccommodationNotFoundException.class)
	public ResponseEntity<Void> handleWishlistAccommodationNotFoundException(
		WishlistAccommodationNotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}

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

	@ExceptionHandler(DuplicatedEmailException.class)
	public ResponseEntity<Void> handleDuplicatedEmailException(DuplicatedEmailException e) {
		log.error("email duplicated: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.CONFLICT).build();
	}

	@ExceptionHandler(NotEqualHostException.class)
	public ResponseEntity<Void> handleNotEqualHostException(NotEqualHostException e) {
		log.error("NotEqualHostException: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Void> handleExceptions(Exception e) {
		log.error("Unhandled exception", e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	}
}
