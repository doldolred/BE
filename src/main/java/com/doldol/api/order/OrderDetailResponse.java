package com.doldol.api.order;

import java.time.Instant;
import java.util.List;

public record OrderDetailResponse(String orderNumber, OrderStatus status, int totalPrice, String receiverName, String receiverPhone, String zipcode, String address1, String address2, String memo, List<Item> items, Instant createdAt) {
    public record Item(Long productId, String productName, int unitPrice, int quantity, int lineTotal) { static Item from(OrderItem item) {return new Item(item.getProduct().getId(), item.getProductName(), item.getUnitPrice(), item.getQuantity(), item.getLineTotal());}}
    public static OrderDetailResponse from(Order order) {return new OrderDetailResponse(order.getOrderNumber(), order.getStatus(), order.getTotalPrice(), order.getReceiverName(), order.getReceiverPhone(), order.getZipcode(), order.getAddress1(), order.getAddress2(), order.getMemo(), order.getItems().stream().map(Item::from).toList(), order.getCreatedAt());
    }
}