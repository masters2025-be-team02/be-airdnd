package kr.kro.airbob.domain.review.exception;

public class ReviewSummaryNotFoundException extends RuntimeException{

	private static final String ERROR_MESSAGE = "존재하지 않는 숙소 리뷰 요약입니다.";

	public ReviewSummaryNotFoundException() {
		super(ERROR_MESSAGE);
	}
}
