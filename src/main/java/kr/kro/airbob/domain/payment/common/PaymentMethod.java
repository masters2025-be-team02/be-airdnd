package kr.kro.airbob.domain.payment.common;

public enum PaymentMethod {
	CREDIT_CARD,
	DEBIT_CARD,
	PAYPAL,
	APPLE_PAY,
	BANK_TRANSFER,
	VIRTUAL_ACCOUNT,
	POINTS,       // 적립금, 포인트
	COUPON        // 전액 쿠폰 사용
}
