package com.doldol.api.payment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	boolean existsByProviderAndPaymentKey(PaymentProvider provider, String paymentKey);

}
