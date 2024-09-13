package com.realtimegroupbuy.rtgb.controller.purchasegroup.dto;

import com.realtimegroupbuy.rtgb.model.PurchaseGroup;
import java.time.LocalDateTime;

public record PurchaseGroupResponse(
    Long groupId,
    String productName,
    Integer targetPurchaseQuantity,
    Integer currentPurchaseQuantity,
    String creatorName,
    LocalDateTime expiresAt,
    String status
) {
    // 엔티티에서 DTO로 변환
    public static PurchaseGroupResponse from(PurchaseGroup purchaseGroup) {
        return new PurchaseGroupResponse(
            purchaseGroup.getId(),
            purchaseGroup.getProduct().getName(),
            purchaseGroup.getTargetPurchaseQuantity(),
            purchaseGroup.getCurrentPurchaseQuantity(),
            purchaseGroup.getCreator().getNickname(),
            purchaseGroup.getExpiresAt(),
            purchaseGroup.getStatus().name()
        );
    }
}

