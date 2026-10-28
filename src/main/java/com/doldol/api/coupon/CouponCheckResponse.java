package com.doldol.api.coupon;

public record CouponCheckResponse(String code, String name, int itemsPrice, int discountAmount) {
}
