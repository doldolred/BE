package com.doldol.api.admin.product;

import com.doldol.api.product.Product;
import java.time.Instant;

public record AdminProductResponse(Long id, String name, String description, int price, int stock, String imageUrl, boolean active, Instant createdAt, Instant updatedAt) {
    public static AdminProductResponse from(Product p) {return new AdminProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStock(), p.getImageUrl(), p.isActive(), p.getCreatedAt(), p.getUpdatedAt());}
}