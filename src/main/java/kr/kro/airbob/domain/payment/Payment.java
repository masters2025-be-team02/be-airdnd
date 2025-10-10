package kr.kro.airbob.domain.payment;

import jakarta.persistence.*;
import kr.kro.airbob.common.domain.BaseEntity;
import kr.kro.airbob.domain.member.Member;
import kr.kro.airbob.domain.payment.common.PaymentMethod;
import kr.kro.airbob.domain.payment.common.PaymentStatus;
import kr.kro.airbob.domain.reservation.entity.Reservation;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

	@Id
	private byte[] paymentId;

	@Column(nullable = false, unique = true)
	private String tossPaymentKey;

	// 토스내부에서 관리하는 별도의 orderId가 존재함
	@Column(nullable = false)
	private String tossOrderId;

	private long totalAmount;

	@Enumerated(EnumType.STRING)
	private PaymentMethod paymentMethod;

	@Enumerated(EnumType.STRING)
	private PaymentStatus status;

	@Column(nullable = false)
	private LocalDateTime requestedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reservation_id")
	private Reservation reservation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;
}
