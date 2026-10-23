package com.doldol.api.admin.order;

import com.doldol.api.order.Order;
import com.doldol.api.order.OrderDetailResponse;
import java.time.Instant;

/** 관리자 주문 상세 — 고객이 보는 주문 상세 + 주문한 회원 + 배송 정보 */
public record AdminOrderDetailResponse(OrderDetailResponse order, Long memberId, String memberEmail,
                                       String memberName, String courier, String trackingNumber, Instant shippedAt, Instant deliveredAt) {

    public static AdminOrderDetailResponse from(Order o) {
        return new AdminOrderDetailResponse(OrderDetailResponse.from(o), o.getMember().getId(),
                o.getMember().getEmail(), o.getMember().getName(), o.getCourier(), o.getTrackingNumber(),
                o.getShippedAt(), o.getDeliveredAt());
    }

}