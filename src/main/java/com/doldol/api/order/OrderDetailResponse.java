package com.doldol.api.order;

import com.doldol.api.payment.Payment;
import com.doldol.api.payment.PaymentProvider;
import com.doldol.api.payment.PaymentStatus;
import java.time.Instant;
import java.util.List;

public record OrderDetailResponse(String orderNumber, OrderStatus status, int totalPrice, String receiverName, String receiverPhone, String zipcode, String address1, String address2, String memo, List<Item> items, PaymentInfo payment, Instant createdAt) {
    public record Item(Long productId, String productName, int unitPrice, int quantity, int lineTotal) { static Item from(OrderItem item) {return new Item(item.getProduct().getId(), item.getProductName(), item.getUnitPrice(), item.getQuantity(), item.getLineTotal());}}

    /** 승인된(또는 승인 후 취소된) 결제. 결제 전이면 null */
    public record PaymentInfo(PaymentProvider provider, PaymentStatus status, String method, int amount, Instant approvedAt, Instant cancelledAt, String receiptUrl) {
        static PaymentInfo from(Payment p) {return new PaymentInfo(p.getProvider(), p.getStatus(), p.getMethod(), p.getAmount(), p.getApprovedAt(), p.getCancelledAt(), p.getReceiptUrl());}
    }

    public static OrderDetailResponse from(Order order) {
        PaymentInfo payment = order.getPayments().stream().filter(p -> p.getStatus() != PaymentStatus.FAILED).findFirst().map(PaymentInfo::from).orElse(null);
        return new OrderDetailResponse(order.getOrderNumber(), order.getStatus(), order.getTotalPrice(), order.getReceiverName(), order.getReceiverPhone(), order.getZipcode(), order.getAddress1(), order.getAddress2(), order.getMemo(), order.getItems().stream().map(Item::from).toList(), payment, order.getCreatedAt());
    }
}
