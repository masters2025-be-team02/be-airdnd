package kr.kro.airbob.domain.member.exception;

public class DuplicatedEmailException extends RuntimeException{

	public static final String ERROR_MESSAGE = "이미 존재하는 이메일입니다.";

	public DuplicatedEmailException() {
		super(ERROR_MESSAGE);
	}
}
