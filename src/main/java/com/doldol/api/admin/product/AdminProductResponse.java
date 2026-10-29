package com.doldol.api.admin.product;

import com.doldol.api.product.Product;
import com.doldol.api.product.ProductOption;
import com.doldol.api.product.ProductStatus;
import java.time.Instant;
import java.util.List;

/** 관리자용 — 판매중지된 옵션까지 전부 + 라인업 항목 */
public record AdminProductResponse(Long id, String name, String description, String imageUrl, ProductStatus status, List<Option> options,
                                   String enName, String spec, List<String> points, String theme, String art, String artFrontUrl, String artBackUrl,
                                   int lineupOrder, Instant createdAt, Instant updatedAt) {

    public record Option(Long id, String name, int listPrice, int price, int discountRate, int stock, int sortOrder, boolean active) {
        static Option from(ProductOption o) {
            return new Option(o.getId(), o.getName(), o.getListPrice(), o.getPrice(), o.getDiscountRate(), o.getStock(), o.getSortOrder(), o.isActive());
        }
    }

    public static AdminProductResponse from(Product p) {
        return new AdminProductResponse(p.getId(), p.getName(), p.getDescription(), p.getImageUrl(), p.getStatus(),
                p.getOptions().stream().map(Option::from).toList(), p.getEnName(), p.getSpec(), p.getPointList(), p.getTheme(), p.getArt(),
                p.getArtFrontUrl(), p.getArtBackUrl(), p.getLineupOrder(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
