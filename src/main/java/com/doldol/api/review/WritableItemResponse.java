package com.doldol.api.review;

import com.doldol.api.order.OrderItem;
import java.time.Instant;

public record WritableItemResponse(Long orderItemId, String orderNumber, Long productId, String productName, String optionName,
                                   Instant deliveredAt) {

    public static WritableItemResponse from(OrderItem item) {
        return new WritableItemResponse(item.getId(), item.getOrder().getOrderNumber(), item.getProduct().getId(), item.getProductName(),
                item.getOptionName(), item.getOrder().getDeliveredAt());
    }

}