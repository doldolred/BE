package com.doldol.api.payment;

public enum PaymentStatus {

	PAID,
	FAILED,
	CANCELLED,
	/** 일부만 환불 (반품 배송비를 뺀 반품 환불 등) — V12 */
	PARTIAL_CANCELLED

}
