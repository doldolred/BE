package com.doldol.api.admin.product;

import com.doldol.api.product.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeStatusRequest(@NotNull(message = "상태를 선택해 주세요.") ProductStatus status) {
}
