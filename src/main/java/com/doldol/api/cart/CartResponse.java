package com.doldol.api.cart;

import com.doldol.api.product.ProductOption;
import com.doldol.api.shipping.ShippingQuote;
import java.util.List;

/**
 * 장바구니. shippingFee 는 기본 배송비 미리보기 — 제주 · 도서 추가 배송비는 주문서에서 우편번호로 계산한다.
 * totalPrice = itemsPrice + shippingFee
 */
public record CartResponse(List<Item> items, int itemsPrice, int shippingFee, int freeShippingThreshold, int totalPrice) {

    public record Item(Long id, Long productId, String productName, Long optionId, String optionName, String imageUrl,
                       int unitPrice, int quantity, int lineTotal, int stock, boolean onSale) {
        static Item from(CartItem cartItem) {
            ProductOption o = cartItem.getOption();
            var p = o.getProduct();
            return new Item(cartItem.getId(), p.getId(), p.getName(), o.getId(), o.getName(), p.getImageUrl(), o.getPrice(),
                    cartItem.getQuantity(), cartItem.getLineTotal(), o.getStock(), o.isOnSale());
        }}

    public static List<Item> items(List<CartItem> cartItems) {
        return cartItems.stream().map(Item::from).toList();
    }

    public static CartResponse of(List<Item> items, ShippingQuote shipping) {
        int itemsPrice = items.stream().mapToInt(Item::lineTotal).sum();
        int fee = items.isEmpty() ? 0 : shipping.fee();
        return new CartResponse(items, itemsPrice, fee, shipping.freeThreshold(), itemsPrice + fee);
    }

}
