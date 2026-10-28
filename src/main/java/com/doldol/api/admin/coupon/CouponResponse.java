package com.doldol.api.admin.coupon;

import com.doldol.api.coupon.Coupon;
import com.doldol.api.coupon.DiscountType;

import java.time.Instant;

public record CouponResponse(Long id, String code, String name, DiscountType discountType, int discountValue, Integer maxDiscount, int minOrderAmount, Instant startsAt, Instant endsAt, Integer usageLimit, int perMemberLimit, int usedCount, boolean active, Instant createdAt) {
    public static CouponResponse from(Coupon c){
        return new CouponResponse(c.getId(), c.getCode(), c.getName(), c.getDiscountType(), c.getDiscountValue(), c.getMaxDiscount(), c.getMinOrderAmount(), c.getStartsAt(), c.getEndsAt(), c.getUsageLimit(), c.getPerMemberLimit(), c.getUsedCount(), c.isActive(), c.getCreatedAt());

    }
}
