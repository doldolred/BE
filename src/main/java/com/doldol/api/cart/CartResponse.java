package com.doldol.api.cart;

import com.doldol.api.product.Product;
import java.util.List;

public record CartResponse(List<Item> items, int totalPrice) {

    public record Item(Long id, Long productId, String productName, String imageUrl, int unitPrice, int quantity, int lineTotal) {
        static Item from(CartItem cartItem) {
            Product p = cartItem.getProduct();
            return new Item(cartItem.getId(), p.getId(), p.getName(), p.getImageUrl(), p.getPrice(), cartItem.getQuantity(), cartItem.getLineTotal());
        }}
    public static CartResponse from(List<CartItem> cartItems) {
        List<Item> items = cartItems.stream().map(Item::from).toList();
        int total = items.stream().mapToInt(Item::lineTotal).sum();
        return new CartResponse(items, total);
    }

}