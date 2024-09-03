package com.realtimegroupbuy.rtgb.controller.groupbuy.dto;

import java.time.LocalDateTime;

public record GroupBuyCreateRequest(
    Long productId,
    Integer targetQuantity, // 목표 구매 수량
    LocalDateTime expiresAt // 종료 시간
) {
}
