package com.realtimegroupbuy.rtgb.model.enums;

public enum GroupBuyStatus {
    IN_PROGRESS, // 공동구매 진행 중 - [유저 참여 가능]
    COMPLETED, // 공동 구매 완료(성공) - 목표 인원 수 달성하여 진행 완료 [유저 참여 불가능]
    FAILED // 실패 - 공동 구매 실패 - 목표 인원을 달성하지 못했거나, 기간이 종료되어 진행 불가 [유저 참여 불가능]
}

