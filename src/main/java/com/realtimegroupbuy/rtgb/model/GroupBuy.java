package com.realtimegroupbuy.rtgb.model;

import com.realtimegroupbuy.rtgb.model.enums.GroupBuyStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Data
public class GroupBuy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer targetCount; // 공동 구매 모집 인원
    private Integer currentCount; // 현재 공동 구매 참여 인원
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    private GroupBuyStatus status;
}
