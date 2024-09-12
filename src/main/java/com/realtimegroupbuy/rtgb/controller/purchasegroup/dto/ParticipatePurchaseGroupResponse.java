package com.realtimegroupbuy.rtgb.controller.purchasegroup.dto;

import com.realtimegroupbuy.rtgb.model.Order;
import com.realtimegroupbuy.rtgb.model.enums.OrderStatus;

public record ParticipatePurchaseGroupResponse(
    Long purchaseGroupId,
    Integer quantity,
    Double totalAmount,
    OrderStatus status
) {
    public static ParticipatePurchaseGroupResponse from(Order order) {
        return new ParticipatePurchaseGroupResponse(
            order.getPurchaseGroup().getId(),
            order.getQuantity(),
            order.getTotalAmount(),
            order.getStatus()
        );
    }
}
