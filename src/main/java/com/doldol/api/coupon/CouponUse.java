package com.doldol.api.coupon;

import com.doldol.api.member.Member;
import com.doldol.api.order.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "coupon_use")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponUse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;
    @Column(nullable = false)
    private int discountAmount;
    @Column(nullable = false)
    private Instant createdAt;
    public static CouponUse create(Coupon coupon, Member member, Order order, int discountAmount){
        CouponUse use = new CouponUse();
        use.coupon = coupon;
        use.member = member;
        use.order = order;
        use.discountAmount = discountAmount;
        use.createdAt = Instant.now();
        return use;
    }
}
