package com.realtimegroupbuy.rtgb.controller.purchasegroup.dto;

import java.time.LocalDateTime;

public record CreatePurchaseGroupRequest(
    Long productId,
    Integer targetQuantity, // 목표 구매 수량
    LocalDateTime expiresAt // 종료 시간
) {
}
