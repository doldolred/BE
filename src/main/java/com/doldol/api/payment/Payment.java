package com.doldol.api.payment;

import com.doldol.api.common.BaseEntity;
import com.doldol.api.order.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 결제 기록 (V3). 한 주문에 여러 번 있을 수 있다 — 실패 후 재결제 */
@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PaymentProvider provider;

	/** 나이스페이 tid */
	@Column(nullable = false, length = 100)
	private String paymentKey;

	@Column(length = 30)
	private String method;

	@Column(nullable = false)
	private int amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PaymentStatus status;

	@Column(length = 500)
	private String receiptUrl;

	private String failReason;

	private Instant approvedAt;

	private Instant cancelledAt;

	/** 나이스페이가 승인한 결제 */
	public static Payment paid(Order order, NicepayClient.Approval approval) {
		Payment payment = new Payment();
		payment.order = order;
		payment.provider = PaymentProvider.NICEPAY;
		payment.paymentKey = approval.tid();
		payment.method = cut(approval.payMethod(), 30);
		payment.amount = approval.amount() == null ? 0 : approval.amount();
		payment.status = PaymentStatus.PAID;
		payment.receiptUrl = cut(approval.receiptUrl(), 500);
		payment.approvedAt = approval.paidAtInstant();
		return payment;
	}

	/** 승인 거절 — 왜 실패했는지 남겨 둔다 (관리자 확인용) */
	public static Payment failed(Order order, String tid, int amount, String reason) {
		Payment payment = new Payment();
		payment.order = order;
		payment.provider = PaymentProvider.NICEPAY;
		payment.paymentKey = tid;
		payment.amount = amount;
		payment.status = PaymentStatus.FAILED;
		payment.failReason = cut(reason, 255);
		return payment;
	}

	public void cancel() {
		status = PaymentStatus.CANCELLED;
		cancelledAt = Instant.now();
	}

	private static String cut(String value, int max) {
		return value == null || value.length() <= max ? value : value.substring(0, max);
	}

}
