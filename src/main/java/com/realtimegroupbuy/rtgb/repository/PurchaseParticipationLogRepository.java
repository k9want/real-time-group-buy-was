package com.realtimegroupbuy.rtgb.repository;

import com.realtimegroupbuy.rtgb.model.PurchaseParticipationLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseParticipationLogRepository extends JpaRepository<PurchaseParticipationLog, Long> {

    // 특정 requestId로 로그가 존재하는지 확인합니다.
    boolean existsByRequestId(UUID requestId);
}
