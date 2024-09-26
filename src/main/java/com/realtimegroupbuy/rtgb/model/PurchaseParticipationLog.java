package com.realtimegroupbuy.rtgb.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseParticipationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private UUID requestId;        // 메시지 고유 식별자
    private Long userId;           // 사용자 ID
    private Long purchaseGroupId;  // 공동 구매 그룹 ID
    private Long orderId;          // 생성된 주문 ID
    private LocalDateTime timestamp; // 처리 시각
}