package com.realtimegroupbuy.rtgb.model;

import com.realtimegroupbuy.rtgb.model.enums.ProductStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    // 재고 차감
    public void discountStock(Integer targetQuantity) {
        // 제품 재고 파악
        if (this.stock < targetQuantity) {
            throw new IllegalArgumentException("현재 제품의 재고보다 많은 수량을 지정했습니다.");
        }

        this.stock -= targetQuantity;

        if (this.stock == 0) {
            this.status = ProductStatus.SOLDOUT;
        }
    }
}
