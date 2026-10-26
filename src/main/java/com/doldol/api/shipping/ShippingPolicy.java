package com.doldol.api.shipping;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 배송비 규칙 — 기본 3,000원, 상품 금액 50,000원 이상 무료 (application.yml app.shipping).
 * 제주 · 도서 추가 배송비는 무료배송이어도 붙는다 (우편번호로 shipping_area 에서 찾는다).
 */
@Component
@Transactional(readOnly = true)
public class ShippingPolicy {

    private final ShippingAreaRepository shippingAreaRepository;

    private final int baseFee;

    private final int freeThreshold;

    public ShippingPolicy(ShippingAreaRepository shippingAreaRepository,
            @Value("${app.shipping.base-fee:3000}") int baseFee,
            @Value("${app.shipping.free-threshold:50000}") int freeThreshold) {
        this.shippingAreaRepository = shippingAreaRepository;
        this.baseFee = baseFee;
        this.freeThreshold = freeThreshold;
    }

    /** zipcode 가 비어 있으면 추가 배송비 없이 (장바구니 미리보기) */
    public ShippingQuote quote(int itemsPrice, String zipcode) {
        int base = itemsPrice >= freeThreshold ? 0 : baseFee;
        ShippingArea area = zipcode == null || zipcode.isBlank() ? null
                : shippingAreaRepository.findMatching(zipcode.trim()).stream().findFirst().orElse(null);
        int extra = area == null ? 0 : area.getExtraFee();
        return new ShippingQuote(base, extra, area == null ? null : area.getName(), base + extra, freeThreshold);
    }

}
