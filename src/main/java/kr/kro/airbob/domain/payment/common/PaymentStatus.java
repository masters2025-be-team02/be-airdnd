package kr.kro.airbob.domain.payment.common;

public enum PaymentStatus {
	PENDING,         // 결제 대기 중
	COMPLETED,       // 결제 완료
	FAILED,          // 결제 실패
	CANCELLED,       // 결제 취소
	REFUNDED,        // 환불 완료
	PARTIALLY_REFUNDED, // 부분 환불
	EXPIRED          // 결제 만료
}
