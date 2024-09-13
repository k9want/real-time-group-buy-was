package com.realtimegroupbuy.rtgb.controller.purchasegroup.dto;

import com.realtimegroupbuy.rtgb.model.PurchaseGroup;
import com.realtimegroupbuy.rtgb.model.enums.PurchaseGroupStatus;
import java.time.LocalDateTime;

public record CreatePurchaseGroupResponse(
    Long groupBuyId,
    Long productId,
    String productName,
    String creator,
    Integer targetPurchaseQuantity, // 목표 구매 수량
    Integer currentPurchaseQuantityCount, // 현재 구매 수량 : 0
    LocalDateTime expiresAt, // 종료 시간
    PurchaseGroupStatus status // IN_PROGRESS - 공동구매 진행 중
) {
    public static CreatePurchaseGroupResponse from(PurchaseGroup purchaseGroup) {
        return new CreatePurchaseGroupResponse(
            purchaseGroup.getId(),
            purchaseGroup.getProduct().getId(),
            purchaseGroup.getProduct().getName(),
            purchaseGroup.getCreator().getNickname(),
            purchaseGroup.getTargetPurchaseQuantity(),
            purchaseGroup.getCurrentPurchaseQuantity(),
            purchaseGroup.getExpiresAt(),
            purchaseGroup.getStatus()
        );
    }
}
