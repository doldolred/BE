package com.doldol.api.admin.product;

import jakarta.validation.constraints.NotNull;

public record ChangeActiveRequest(@NotNull(message = "판매 여부를 선택해 주세요.") Boolean active) {
}
