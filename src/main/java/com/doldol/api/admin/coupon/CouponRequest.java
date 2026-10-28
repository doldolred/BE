package com.doldol.api.admin.coupon;

import com.doldol.api.coupon.DiscountType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CouponRequest(
        @NotBlank(message = "코드를 입력해 주세요.")
        @Pattern(regexp = "^\\s*[A-Za-z0-9_-]{3,30}\\s*$", message = "코드는 영문 · 숫자 · - · _ 3~30자예요.")
        String code,
        @NotBlank(message = "이름을 입력해 주세요.")
        @Size(max = 50, message = "이름은 50자 이하로 입력해 주세요.")
        String name,
        @NotNull(message = "할인 방식을 골라 주세요.")
        DiscountType discountType,
        @NotNull(message = "할인 값을 입력해 주세요.")
        @Min(value = 1, message = "할인 값은 1 이상이에요.")
        @Max(value = 1_000_000, message = "할인 값이 너무 커요.")
        Integer discountValue,
        // 정률일 때만 (최대 할인 원). 비우면 상한 없음
        @Min(value = 100, message = "최대 할인은 100원 이상이에요.")
        Integer maxDiscount,
        @NotNull(message = "최소 주문 금액을 입력해 주세요.")
        @Min(value = 0, message = "최소 주문 금액은 0원 이상이에요.")
        Integer minOrderAmount,
        Instant startsAt,
        Instant endsAt,
        // 비우면 무제한
        @Min(value = 1, message = "전체 수량은 1 이상이에요.")
        Integer usageLimit,
        @NotNull(message = "한 사람당 사용 횟수를 입력해 주세요.")
        @Min(value = 1, message = "한 사람당 1번 이상이에요.")
        @Max(value = 100, message = "한 사람당 100번까지예요.")
        Integer perMemberLimit,
        @NotNull(message = "사용 여부를 골라 주세요.")
        Boolean active) {

}