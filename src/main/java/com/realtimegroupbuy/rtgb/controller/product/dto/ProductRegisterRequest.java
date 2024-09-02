package com.realtimegroupbuy.rtgb.controller.product.dto;

public record ProductRegisterRequest(
    String name,
    String description,
    Double price,
    Integer stock,
    String category
) {

}
