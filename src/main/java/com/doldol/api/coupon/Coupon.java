package com.doldol.api.coupon;

import com.doldol.api.common.BaseEntity;
import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {
    public static final int MIN_PAYMENT = 1_000;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 30)
    private String code;
    @Column(nullable = false, length = 50)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DiscountType discountType;
    @Column(nullable = false)
    private int discountValue;
    private Integer maxDiscount;
    @Column(nullable = false)
    private int minOrderAmount;
    private Instant startsAt;
    private Instant endsAt;
    private Integer usageLimit;
    @Column(nullable = false)
    private int perMemberLimit;
    @Column(nullable = false)
    private int usedCount;
    @Column(nullable = false)
    private boolean active;

    public static Coupon create(String code, String name, DiscountType type, int value, Integer maxDiscount, int minOrderAmount, Instant startsAt, Instant endsAt, Integer usageLimit, int perMemberLimit, boolean active) {
        Coupon coupon = new Coupon();
        coupon.code = normalize(code);
        coupon.update(name, type, value, maxDiscount, minOrderAmount, startsAt, endsAt, usageLimit, perMemberLimit, active);
        return coupon;
    }

    public void update(String name, DiscountType type, int value, Integer maxDiscount, int minOrderAmount, Instant startsAt, Instant endsAt, Integer usageLimit, int perMemberLimit, boolean active) {
        this.name = name.trim();
        this.discountType = type;
        this.discountValue = value;
        this.maxDiscount = type == DiscountType.PERCENT ? maxDiscount : null;
        this.minOrderAmount = minOrderAmount;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.usageLimit = usageLimit;
        this.perMemberLimit = perMemberLimit;
        this.active = active;
    }

    public static String normalize(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }

    public int discountFor(int itemsPrice, long usedByMember, Instant now) {
        if (!active) {throw unusable("사용할 수 없는 할인코드예요.");}
        if (startsAt != null && now.isBefore(startsAt)) {throw unusable("아직 사용 기간이 아니에요.");}
        if (endsAt != null && !now.isBefore(endsAt)) {throw unusable("사용 기간이 끝난 할인코드예요.");}
        if (usageLimit != null && usedCount >= usageLimit) {throw unusable("준비된 수량이 모두 소진됐어요.");}
        if (usedByMember >= perMemberLimit) {throw unusable("이미 사용한 할인코드예요.");}
        if (itemsPrice < minOrderAmount) {throw unusable("상품 금액 %,d원 이상부터 쓸 수 있어요.".formatted(minOrderAmount));}
        int raw = discountType == DiscountType.AMOUNT ? discountValue : (int) ((long) itemsPrice * discountValue / 100);
        if (discountType == DiscountType.PERCENT && maxDiscount != null) {raw = Math.min(raw, maxDiscount);}
        return Math.max(0, Math.min(raw, itemsPrice - MIN_PAYMENT));
    }
    public void use(){usedCount++;}
    public void release(){usedCount = Math.max(0, usedCount - 1);}
    private static BusinessException unusable(String message){return new BusinessException(ErrorCode.COUPON_NOT_USABLE, message);}
}