package com.doldol.api.order;

import java.time.Instant;

public record OrderSummaryResponse(String orderNumber, OrderStatus status, int totalPrice, String itemSummary, Instant createdAt) {
    public static OrderSummaryResponse from(Order order){
        var items = order.getItems();
        String summary = items.isEmpty() ? "" : items.get(0).getDisplayName() + (items.size() > 1 ? " 외 " + (items.size() - 1) + "건" : "");
        return new OrderSummaryResponse(order.getOrderNumber(), order.getStatus(), order.getTotalPrice(), summary, order.getCreatedAt());
    }
}