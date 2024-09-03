package com.realtimegroupbuy.rtgb.controller.product.dto;

import com.realtimegroupbuy.rtgb.model.Product;

public record ProductResponse(
    Long productId,
    String name,
    String description,
    Double price,
    Integer stock,
    String category,
    String seller
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock(),
            product.getCategory(),
            product.getSeller().getNickname()
        );
    }
}
