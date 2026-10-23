package com.doldol.api.admin.order;

import com.doldol.api.order.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangeOrderStatusRequest(
        @NotNull(message = "바꿀 상태를 선택해 주세요.")
        OrderStatus status,
        @Size(max = 30, message ="택배사 이름이 너무 깁니다.")
        String courier,
        @Size(max = 50, message = "송장번호가 너무 깁니다.")
        String trackingNumber
) {
}
