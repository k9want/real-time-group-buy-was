package com.realtimegroupbuy.rtgb.messaging;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseParticipationRequest{

    private UUID requestId;          // 고유 식별자 (멱등성 보장용)
    private Long userId;             // 사용자 ID
    private Long purchaseGroupId;    // 공동 구매 그룹 ID
    private Integer orderQuantity;   // 주문 수량

}
