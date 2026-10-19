package com.doldol.api.product;

public record ProductResponse(Long id, String name, String description, int price, int stock, String imageUrl) {
    public static ProductResponse from(Product product){
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice(), product.getStock(), product.getImageUrl());
    }
}
