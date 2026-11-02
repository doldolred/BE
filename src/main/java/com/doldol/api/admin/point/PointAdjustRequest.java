package com.doldol.api.admin.point;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PointAdjustRequest(
        @NotNull(message = "금액을 입력해 주세요.")
        @Min(value = -1_000_000, message = "한 번에 100만 원까지 차감할 수 있어요.")
        @Max(value = 1_000_000, message = "한 번에 100만 원까지 지급할 수 있어요.")
        Integer amount,
        @NotBlank(message = "사유를 입력해 주세요.")
        @Size(max = 100, message = "사유는 100자 이하로 입력해 주세요.")
        String memo) {}