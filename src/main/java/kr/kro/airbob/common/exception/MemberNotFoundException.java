package kr.kro.airbob.common.exception;

public class MemberNotFoundException extends RuntimeException{

	public static final String ERROR_MESSAGE = "존재하지 않는 사용자입니다.";

	public MemberNotFoundException() {
		super(ERROR_MESSAGE);
	}
}
