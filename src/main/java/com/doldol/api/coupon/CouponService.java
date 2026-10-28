package com.doldol.api.coupon;

import com.doldol.api.admin.coupon.CouponRequest;
import com.doldol.api.admin.coupon.CouponResponse;
import com.doldol.api.cart.CartItem;
import com.doldol.api.cart.CartItemRepository;
import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.member.Member;
import com.doldol.api.order.Order;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    private final CouponUseRepository couponUseRepository;

    private final CartItemRepository cartItemRepository;

    // ── 손님 ─────────────────────────────

    @Transactional(readOnly = true)
    public CouponCheckResponse check(Long memberId, String code) {
        List<CartItem> cart = cartItemRepository.findAllWithProductByMemberId(memberId);
        if (cart.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }
        int itemsPrice = cart.stream().mapToInt(CartItem::getLineTotal).sum();
        Coupon coupon = couponRepository.findByCode(Coupon.normalize(code)).orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
        int discount = coupon.discountFor(itemsPrice, couponUseRepository.countByCouponIdAndMemberId(coupon.getId(), memberId), Instant.now());
        return new CouponCheckResponse(coupon.getCode(), coupon.getName(), itemsPrice, discount);
    }

    public void apply(Order order, Member member, String code) {
        Coupon coupon = couponRepository.findForUpdate(Coupon.normalize(code)).orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
        int discount = coupon.discountFor(order.getItemsPrice(), couponUseRepository.countByCouponIdAndMemberId(coupon.getId(), member.getId()),
                Instant.now());
        coupon.use();
        order.applyDiscount(coupon.getCode(), discount);
        couponUseRepository.save(CouponUse.create(coupon, member, order, discount));
    }

    public void release(Order order) {
        couponUseRepository.findByOrderId(order.getId()).ifPresent(use -> {
            couponRepository.findForUpdate(use.getCoupon().getCode()).ifPresent(Coupon::release);
            couponUseRepository.delete(use);
        });
    }

    // ── 관리자 ───────────────────────────

    @Transactional(readOnly = true)
    public List<CouponResponse> getAll() {
        return couponRepository.findAllByOrderByIdDesc().stream().map(CouponResponse::from).toList();
    }

    public CouponResponse create(CouponRequest r) {
        String code = Coupon.normalize(r.code());
        if (couponRepository.existsByCode(code)) {
            throw new BusinessException(ErrorCode.DUPLICATE_COUPON_CODE);
        }
        validate(r);
        Coupon coupon = Coupon.create(code, r.name(), r.discountType(), r.discountValue(), r.maxDiscount(), r.minOrderAmount(), r.startsAt(),
                r.endsAt(), r.usageLimit(), r.perMemberLimit(), r.active());
        return CouponResponse.from(couponRepository.saveAndFlush(coupon));
    }
    public CouponResponse update(Long id, CouponRequest r) {
        Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
        validate(r);
        coupon.update(r.name(), r.discountType(), r.discountValue(), r.maxDiscount(), r.minOrderAmount(), r.startsAt(), r.endsAt(), r.usageLimit(),
                r.perMemberLimit(), r.active());
        return CouponResponse.from(couponRepository.saveAndFlush(coupon));
    }

    public void delete(Long id) {
        Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
        if (coupon.getUsedCount() > 0 || couponUseRepository.existsByCouponId(id)) {
            throw new BusinessException(ErrorCode.COUPON_IN_USE);
        }
        couponRepository.delete(coupon);
    }

    private static void validate(CouponRequest r) {
        if (r.discountType() == DiscountType.PERCENT && r.discountValue() > 90) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "정률 할인은 90%까지예요.");
        }
        if (r.startsAt() != null && r.endsAt() != null && !r.endsAt().isAfter(r.startsAt())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "끝나는 때가 시작보다 뒤여야 해요.");
        }
    }

}