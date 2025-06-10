package kr.kro.airbob.domain.payment.common;

public enum PaymentGateway {
	KAKAO_PAY,
	NAVER_PAY,
	TOSS,
	PAYPAL,
	STRIPE,
	NICE,
	KCP,
	DIRECT      // PG 안 거치는 직접 결제 (ex. 현장)
}
