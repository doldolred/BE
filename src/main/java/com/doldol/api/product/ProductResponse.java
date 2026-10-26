package com.doldol.api.product;

import java.util.List;

/** 손님용 상품 — 판매중인 옵션만. minPrice 는 목록 카드에 "38,000원~" 처럼 보여 줄 값 (품절 옵션은 빼고, 다 품절이면 전체 중) */
public record ProductResponse(Long id, String name, String description, String imageUrl, int minPrice, boolean soldOut, List<Option> options) {

    public record Option(Long id, String name, int listPrice, int price, int discountRate, int stock, boolean soldOut) {
        static Option from(ProductOption o) {
            return new Option(o.getId(), o.getName(), o.getListPrice(), o.getPrice(), o.getDiscountRate(), o.getStock(), o.isSoldOut());
        }
    }

    public static ProductResponse from(Product product){
        List<Option> options = product.getActiveOptions().stream().map(Option::from).toList();
        int minPrice = options.stream().filter(o -> !o.soldOut()).mapToInt(Option::price).min()
                .orElse(options.stream().mapToInt(Option::price).min().orElse(0));
        boolean soldOut = options.stream().allMatch(Option::soldOut);
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getImageUrl(), minPrice, soldOut, options);
    }
}
