package com.realtimegroupbuy.rtgb.repository;

import com.realtimegroupbuy.rtgb.model.PurchaseGroup;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseGroupRepository extends JpaRepository<PurchaseGroup, Long> {

    // 비관적 락(Pessimistic Lock)을 사용하여 공동 구매 그룹을 조회하는 메서드
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pg FROM PurchaseGroup pg WHERE pg.id = :id")
    Optional<PurchaseGroup> findByIdWithPessimisticLock(@Param("id") Long id);

}
