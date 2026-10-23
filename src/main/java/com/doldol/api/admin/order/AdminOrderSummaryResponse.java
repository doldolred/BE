package com.doldol.api.admin.order;

import com.doldol.api.order.Order;
import com.doldol.api.order.OrderStatus;
import java.time.Instant;

public record AdminOrderSummaryResponse(String orderNumber, OrderStatus status, int totalPrice, String itemSummary, String memberEmail, String memberName, String receiverName, Instant createdAt) {

    public static AdminOrderSummaryResponse from(Order o) {
        var items = o.getItems();
        String summary = items.isEmpty() ? ""
                : items.get(0).getProductName() + (items.size() > 1 ? " 외 " + (items.size() - 1) + "건" : "");
        return new AdminOrderSummaryResponse(o.getOrderNumber(), o.getStatus(), o.getTotalPrice(), summary,
                o.getMember().getEmail(), o.getMember().getName(), o.getReceiverName(), o.getCreatedAt());
    }

}