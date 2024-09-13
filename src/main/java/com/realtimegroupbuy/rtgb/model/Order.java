package com.realtimegroupbuy.rtgb.model;

import com.realtimegroupbuy.rtgb.model.enums.OrderStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_group_id")
    private PurchaseGroup purchaseGroup;

    private Integer quantity;
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Builder
    public Order(Long id, User user, PurchaseGroup purchaseGroup, Integer quantity, Double totalAmount, OrderStatus status) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("주문 수량은 1개 이상이어야 합니다.");
        }

        this.id = id;
        this.user = user;
        this.purchaseGroup = purchaseGroup;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    // 주문 총 금약 계산 (상품 가격 * 수량)
    public void calculateTotalAmount() {
        this.totalAmount = this.purchaseGroup.getProduct().getPrice() * this.quantity;
    }

    // 결제
    public void completePayment() {
        if (this.status == OrderStatus.PENDING) {
            this.status = OrderStatus.APPROVE;
        } else {
            throw new IllegalStateException("결제가 이미 완료되었습니다.");
        }
    }

    // 공동 구매 주문 성공
    public void successPurchaseGroups() {
        this.status = OrderStatus.SUCCESS;
    }
}