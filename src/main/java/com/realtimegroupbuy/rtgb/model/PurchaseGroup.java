package com.realtimegroupbuy.rtgb.model;

import com.realtimegroupbuy.rtgb.model.enums.PurchaseGroupStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User creator; // 공동 구매 개설자

    private Integer targetPurchaseQuantity; // 목표 구매 수량
    private Integer currentPurchaseQuantity; // 현재 구매 수량
    private LocalDateTime expiresAt; // 종료 시간
    @Enumerated(EnumType.STRING)
    private PurchaseGroupStatus status;

    // 공동 구매 참여 가능 여부 확인 메서드
    public void validatePurchaseGroupParticipation(Integer orderQuantity) {
        if (this.status != PurchaseGroupStatus.IN_PROGRESS) {
            throw new IllegalStateException("현재 공동구매에 참여할 수 없습니다.");
        }

        // 목표 구매 수량 < 현재 구매 수량 + 주문 수량
        if (this.targetPurchaseQuantity < this.currentPurchaseQuantity + orderQuantity) {
            throw new IllegalArgumentException("현재 재고보다 많은 수량을 주문할 수 없습니다.");
        }
    }

    // 구매 진행 상황 업데이트
    public PurchaseGroup updatePurchaseProgress(Integer orderQuantity) {
        this.currentPurchaseQuantity += orderQuantity;

        if (this.targetPurchaseQuantity.equals(this.currentPurchaseQuantity)) {
            this.status = PurchaseGroupStatus.COMPLETED;
        }

        return this;
    }

    // 공동 구매 완료 여부 확인
    public boolean isCompleted() {
        return this.status == PurchaseGroupStatus.COMPLETED;
    }
}
