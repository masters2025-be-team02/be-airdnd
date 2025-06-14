package kr.kro.airbob.domain.payment;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import kr.kro.airbob.common.domain.BaseEntity;
import kr.kro.airbob.domain.member.Member;
import kr.kro.airbob.domain.payment.common.PaymentGateway;
import kr.kro.airbob.domain.payment.common.PaymentMethod;
import kr.kro.airbob.domain.payment.common.PaymentStatus;
import kr.kro.airbob.domain.reservation.Reservation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Integer totalPrice;

	@Enumerated(EnumType.STRING)
	private PaymentMethod paymentMethod;
	@Enumerated(EnumType.STRING)
	private PaymentGateway paymentGateway;
	@Enumerated(EnumType.STRING)
	private PaymentStatus status;
	private Long transactionId;
	private String cancelReason;
	private LocalDateTime paidAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reservation_id")
	private Reservation reservation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;
}
