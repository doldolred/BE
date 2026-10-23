package com.doldol.api.payment;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.order.Order;
import com.doldol.api.order.OrderItem;
import com.doldol.api.order.OrderRepository;
import com.doldol.api.order.OrderStatus;
import com.doldol.api.product.ProductRepository;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 결제 승인 · 환불 · 결제 대기 자동 취소 (나이스페이 START).
 *
 * 나이스페이 호출(네트워크)은 DB 트랜잭션 밖에서 하고, DB 작업만 tx(TransactionTemplate)로 짧게 묶는다.
 * 승인은 됐는데 주문에 반영하지 못하면(그새 자동 취소됨 · 금액 불일치 등) 승인을 바로 취소한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentProperties properties;

	private final OrderRepository orderRepository;

	private final PaymentRepository paymentRepository;

	private final ProductRepository productRepository;

	private final NicepayClient nicepayClient;

	private final TransactionTemplate tx;

	// ───────── 결제 승인 ─────────

	/** 결제창 인증 결과(returnUrl) 처리 → 브라우저를 보낼 FE 주소 (성공: 주문 상세 / 실패: 실패 화면) */
	public String nicepayReturn(NicepayReturnRequest req) {
		String orderNumber = Objects.requireNonNullElse(req.orderId(), "");
		try {
			if (!"0000".equals(req.authResultCode())) {
				throw new BusinessException(ErrorCode.PAYMENT_NOT_COMPLETED,
						Objects.requireNonNullElse(req.authResultMsg(), "결제가 취소되었어요."));
			}
			if (!nicepayClient.verifyAuthSignature(req.authToken(), req.amount(), req.signature())) {
				throw new BusinessException(ErrorCode.INVALID_INPUT, "결제 정보가 올바르지 않아요.");
			}
			int amount = parseAmount(req.amount());
			// 새로고침 등으로 같은 결과가 두 번 오면 이미 결제된 주문으로 보낸다
			if (Boolean.TRUE.equals(tx.execute(s -> checkPayable(orderNumber, amount, req.tid())))) {
				return successUrl(orderNumber);
			}
			NicepayClient.Approval approval = nicepayClient.approve(req.tid(), amount, orderNumber);
			if (!approval.isPaid()) {
				String reason = Objects.requireNonNullElse(approval.resultMsg(), "결제가 승인되지 않았어요.");
				tx.executeWithoutResult(s -> recordFailure(orderNumber, req.tid(), amount, reason));
				throw new BusinessException(ErrorCode.PAYMENT_NOT_COMPLETED, reason);
			}
			applyPaid(orderNumber, approval);
			return successUrl(orderNumber);
		}
		catch (BusinessException e) {
			log.warn("나이스페이 결제 실패 orderNumber={} code={} message={}", orderNumber, e.getErrorCode(),
					e.getMessage());
			return failUrl(orderNumber, e.getMessage());
		}
		catch (RuntimeException e) {
			log.error("나이스페이 결제 처리 오류 orderNumber={}", orderNumber, e);
			return failUrl(orderNumber, ErrorCode.INTERNAL_ERROR.getMessage());
		}
	}

	/** 결제해도 되는 주문인가. 이미 이 결제로 결제된 주문이면 true */
	private boolean checkPayable(String orderNumber, int amount, String tid) {
		Order order = findOrder(orderNumber);
		if (order.isPaidWith(tid)) {
			return true;
		}
		if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
			throw new BusinessException(ErrorCode.ORDER_NOT_PAYABLE);
		}
		if (order.getTotalPrice() != amount) {
			throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
		}
		return false;
	}

	private void recordFailure(String orderNumber, String tid, int amount, String reason) {
		if (tid == null || paymentRepository.existsByProviderAndPaymentKey(PaymentProvider.NICEPAY, tid)) {
			return;
		}
		paymentRepository.save(Payment.failed(findOrder(orderNumber), tid, amount, reason));
	}

	/** 승인된 결제를 주문에 반영. 못 하면 승인을 취소하고 예외를 그대로 던진다 */
	private void applyPaid(String orderNumber, NicepayClient.Approval approval) {
		try {
			tx.executeWithoutResult(s -> {
				Order order = findOrderForUpdate(orderNumber);
				if (order.isPaidWith(approval.tid())) {
					return; // 이미 반영됨
				}
				if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
					throw new BusinessException(ErrorCode.ORDER_NOT_PAYABLE);
				}
				if (approval.amount() == null || order.getTotalPrice() != approval.amount()) {
					throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
				}
				order.pay(paymentRepository.save(Payment.paid(order, approval)));
			});
		}
		catch (RuntimeException e) {
			log.error("승인된 결제를 주문에 반영하지 못해 승인을 취소합니다 orderNumber={}", orderNumber, e);
			try {
				nicepayClient.cancel(approval.tid(), orderNumber, "주문 반영 실패로 자동 취소");
			}
			catch (RuntimeException cancelError) {
				log.error("자동 취소 실패 — 관리자 확인 필요 orderNumber={} tid={}", orderNumber, approval.tid(),
						cancelError);
			}
			throw e;
		}
	}

	// ───────── 환불 ─────────

	/**
	 * 결제된 주문을 취소할 때 환불 (고객 취소 · 관리자 취소). 부르는 쪽 트랜잭션 안에서 쓴다 —
	 * 환불이 실패하면 예외가 나서 주문 취소도 함께 롤백된다. 결제 기록이 없으면(결제 전) 아무것도 안 한다.
	 */
	public void refund(Order order, String reason) {
		order.getPayments()
			.stream()
			.filter(p -> p.getStatus() == PaymentStatus.PAID)
			.findFirst()
			.ifPresent(p -> {
				nicepayClient.cancel(p.getPaymentKey(), order.getOrderNumber(), reason);
				p.cancel();
			});
	}

	// ───────── 결제 대기 자동 취소 ─────────

	/** pendingTimeout(기본 30분)이 지나도록 결제하지 않은 주문을 취소하고 재고를 돌려놓는다 */
	@Scheduled(fixedDelayString = "${app.payment.expire-check-interval:PT1M}",
			initialDelayString = "${app.payment.expire-check-interval:PT1M}")
	public void expirePendingOrders() {
		Instant before = Instant.now().minus(properties.pendingTimeout());
		List<String> orderNumbers = tx
			.execute(s -> orderRepository.findOrderNumbersByStatusBefore(OrderStatus.PENDING_PAYMENT, before));
		if (orderNumbers == null || orderNumbers.isEmpty()) {
			return;
		}
		for (String orderNumber : orderNumbers) {
			try {
				tx.executeWithoutResult(s -> expire(orderNumber));
			}
			catch (RuntimeException e) {
				log.error("결제 대기 주문 자동 취소 실패 orderNumber={}", orderNumber, e);
			}
		}
		log.info("결제 대기 주문 자동 취소 {}건", orderNumbers.size());
	}

	private void expire(String orderNumber) {
		Order order = findOrderForUpdate(orderNumber);
		if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
			return; // 그새 결제됨
		}
		order.cancel();
		for (OrderItem item : order.getItems()) {
			productRepository.increaseStock(item.getProduct().getId(), item.getQuantity());
		}
	}

	// ───────── 도우미 ─────────

	private Order findOrder(String orderNumber) {
		return orderRepository.findByOrderNumber(orderNumber)
			.orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
	}

	/** 같은 주문을 동시에 바꾸지 않게 잠그고 읽는다 (승인 반영 · 자동 취소가 겹칠 때) */
	private Order findOrderForUpdate(String orderNumber) {
		return orderRepository.findForUpdate(orderNumber)
			.orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
	}

	private static int parseAmount(String amount) {
		try {
			return Integer.parseInt(amount);
		}
		catch (NumberFormatException e) {
			throw new BusinessException(ErrorCode.INVALID_INPUT, "결제 금액이 올바르지 않아요.");
		}
	}

	private String successUrl(String orderNumber) {
		return UriComponentsBuilder.fromUriString(frontendBase() + "/mypage/orders/{orderNumber}?complete=1")
			.encode()
			.buildAndExpand(orderNumber)
			.toUriString();
	}

	private String failUrl(String orderNumber, String message) {
		return UriComponentsBuilder
			.fromUriString(frontendBase() + "/checkout/fail?orderNumber={orderNumber}&message={message}")
			.encode()
			.buildAndExpand(orderNumber, message)
			.toUriString();
	}

	/** 비어 있으면 같은 도메인 기준 상대 경로 */
	private String frontendBase() {
		String url = properties.frontendUrl();
		return url == null || url.isBlank() ? "" : url.replaceAll("/+$", "");
	}

}
