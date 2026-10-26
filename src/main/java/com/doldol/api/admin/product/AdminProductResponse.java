package com.doldol.api.admin.product;

import com.doldol.api.product.Product;
import com.doldol.api.product.ProductOption;
import java.time.Instant;
import java.util.List;

/** 관리자용 — 판매중지된 옵션까지 전부 */
public record AdminProductResponse(Long id, String name, String description, String imageUrl, boolean active, List<Option> options, Instant createdAt, Instant updatedAt) {

    public record Option(Long id, String name, int listPrice, int price, int discountRate, int stock, int sortOrder, boolean active) {
        static Option from(ProductOption o) {
            return new Option(o.getId(), o.getName(), o.getListPrice(), o.getPrice(), o.getDiscountRate(), o.getStock(), o.getSortOrder(), o.isActive());
        }
    }

    public static AdminProductResponse from(Product p) {
        return new AdminProductResponse(p.getId(), p.getName(), p.getDescription(), p.getImageUrl(), p.isActive(),
                p.getOptions().stream().map(Option::from).toList(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
