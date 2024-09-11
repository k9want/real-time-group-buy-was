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
    private Integer currentPurchaseQuantityCount; // 현재 구매 수량
    private LocalDateTime expiresAt; // 종료 시간
    @Enumerated(EnumType.STRING)
    private PurchaseGroupStatus status;
}
