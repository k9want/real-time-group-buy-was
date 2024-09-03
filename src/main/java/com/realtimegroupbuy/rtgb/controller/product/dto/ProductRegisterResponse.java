package com.realtimegroupbuy.rtgb.controller.product.dto;

import com.realtimegroupbuy.rtgb.model.Product;

public record ProductRegisterResponse(
    Long productId
) {

    public static ProductRegisterResponse from(Product product) {
        return new ProductRegisterResponse(
            product.getId()
        );
    }
}
