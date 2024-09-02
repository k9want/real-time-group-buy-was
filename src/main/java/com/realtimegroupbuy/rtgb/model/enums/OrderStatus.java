package com.realtimegroupbuy.rtgb.model.enums;

public enum OrderStatus {
    PENDING, // 주문이 아직 처리되지 않은 상태 [결제, 재고 확인 등이 완료되지 않은 상태]
    COMPLETED, // 완료 - 주문 완료(성공) [결제 성공]
    FAILED // 실패 - 주문 실패 [결제 실패 등으로 인해 주문이 처리되지 않은 상태]
}
