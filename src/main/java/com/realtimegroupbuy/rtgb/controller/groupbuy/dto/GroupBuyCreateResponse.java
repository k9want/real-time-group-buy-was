package com.realtimegroupbuy.rtgb.controller.groupbuy.dto;

import com.realtimegroupbuy.rtgb.model.GroupBuy;
import com.realtimegroupbuy.rtgb.model.enums.GroupBuyStatus;
import java.time.LocalDateTime;

public record GroupBuyCreateResponse(
    Long groupBuyId,
    Long productId,
    String productName,
    String creator,
    Integer targetPurchaseQuantity, // 목표 구매 수량
    Integer currentPurchaseQuantityCount, // 현재 구매 수량 : 0
    LocalDateTime expiresAt, // 종료 시간
    GroupBuyStatus status // IN_PROGRESS - 공동구매 진행 중
) {
    public static GroupBuyCreateResponse from(GroupBuy groupBuy) {
        return new GroupBuyCreateResponse(
            groupBuy.getId(),
            groupBuy.getProduct().getId(),
            groupBuy.getProduct().getName(),
            groupBuy.getCreator().getNickname(),
            groupBuy.getTargetPurchaseQuantity(),
            groupBuy.getCurrentPurchaseQuantityCount(),
            groupBuy.getExpiresAt(),
            groupBuy.getStatus()
        );
    }
}
