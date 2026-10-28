package com.doldol.api.coupon;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponUseRepository extends JpaRepository<CouponUse, Long> {
    long countByCouponIdAndMemberId(Long couponId, Long memberId);
    Optional<CouponUse> findByOrderId(Long orderId);
    boolean existsByCouponId(Long couponId);

}