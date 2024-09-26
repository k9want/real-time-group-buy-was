package com.realtimegroupbuy.rtgb.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseSuccessRequest{
    private static final long serialVersionUID = 1L;

    private Long purchaseGroupId;    // 공동 구매 그룹 ID
}
