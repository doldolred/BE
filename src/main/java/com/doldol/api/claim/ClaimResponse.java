package com.doldol.api.claim;

import com.doldol.api.order.Order;
import com.doldol.api.order.OrderStatus;
import java.time.Instant;
import java.util.List;

public record ClaimResponse(Long id,String orderNumber, OrderStatus orderStatus, int orderTotalPrice, ClaimType type, ClaimReason reason, String detail, List<String> imageUrls, ClaimStatus status, int returnShippingFee, int refundAmount, String rejectReason, String exchangeCourier, String exchangeTrackingNumber, Instant approvedAt, Instant completedAt, Instant createdAt, Long memberId, String memberName, String memberEmail) {
    public static ClaimResponse forMember(OrderClaim c){return of(c, null, null, null);}
    public static ClaimResponse forAdmin(OrderClaim c) {return of(c, c.getMember().getId(), c.getMember().getName(), c.getMember().getEmail());}
    private static ClaimResponse of(OrderClaim c, Long memberId, String memberName, String memberEmail) {Order o = c.getOrder();return new ClaimResponse(c.getId(), o.getOrderNumber(), o.getStatus(), o.getTotalPrice(), c.getType(), c.getReason(), c.getDetail(), List.copyOf(c.getImageUrls()), c.getStatus(), c.getReturnShippingFee(), c.getRefundAmount(), c.getRejectReason(), c.getExchangeCourier(), c.getExchangeTrackingNumber(), c.getApprovedAt(), c.getCompletedAt(), c.getCreatedAt(), memberId, memberName, memberEmail);}
}
