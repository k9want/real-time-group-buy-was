package com.realtimegroupbuy.rtgb.service.purchasegroup;

import com.realtimegroupbuy.rtgb.model.Product;
import com.realtimegroupbuy.rtgb.model.PurchaseGroup;
import com.realtimegroupbuy.rtgb.model.User;
import com.realtimegroupbuy.rtgb.model.enums.ProductStatus;
import com.realtimegroupbuy.rtgb.model.enums.PurchaseGroupStatus;
import com.realtimegroupbuy.rtgb.repository.ProductRepository;
import com.realtimegroupbuy.rtgb.repository.PurchaseGroupRepository;
import com.realtimegroupbuy.rtgb.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PurchaseGroupService {

    private static final int MINIMUM_PURCHASE_GROUP_ORDER_QUANTITY = 10;
    private static final int DEFAULT__CURRENT_PURCHASE_QUANTITY = 0;

    private final UserRepository userRepository;
    private final PurchaseGroupRepository purchaseGroupRepository;
    private final ProductRepository productRepository;

    @Transactional
    public PurchaseGroup create(User user, Long productId, Integer targetQuantity, LocalDateTime expiredAt) {
        // 도메인 (최소 주문 개수: 10개 이상, 최소 유효 기간: 1일)
        if (targetQuantity < MINIMUM_PURCHASE_GROUP_ORDER_QUANTITY) {
            throw new IllegalArgumentException("최소 공동 구매 수량은 10개 이상입니다.");
        }

        if (expiredAt.isBefore(LocalDateTime.now().plusDays(1))) {
            throw new IllegalArgumentException("최소 공동 구매 유효 기간은 1일입니다.");
        }
        // 사용자(공동 구매 개설자) 조회
        User creator = userRepository.findById(user.getId())
            .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        // 제품 조회
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        // 제품 구매 가능 여부 확인
        if (product.getStatus() != ProductStatus.AVAILABLE) {
            throw new IllegalArgumentException("해당 제품은 현재 판매 중단 상태이거나 품절입니다.");
        }

        // 제품 재고 차감 - 공동 구매 그룹을 위해 미리 재고 확보 (동시성 문제 발생 지점)
        product.discountStock(targetQuantity);
        productRepository.save(product);

        // 공동구매 그룹 생성
        PurchaseGroup purchaseGroup = PurchaseGroup.builder()
            .product(product)
            .creator(creator)
            .targetPurchaseQuantity(targetQuantity)
            .currentPurchaseQuantity(DEFAULT__CURRENT_PURCHASE_QUANTITY)
            .expiresAt(expiredAt)
            .status(PurchaseGroupStatus.IN_PROGRESS)
            .build();

        return purchaseGroupRepository.save(purchaseGroup);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseGroup> getAllPurchaseGroups(Pageable pageable) {
        return purchaseGroupRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public PurchaseGroup getPurchaseGroup(Long purchaseGroupId) {
        return findById(purchaseGroupId);
    }

    // 공동 구매 참여
    @Transactional
    public PurchaseGroup participateInPurchaseGroup(Long purchaseGroupId, Integer orderQuantity) {
        // 공동 구매 그룹 조회
        PurchaseGroup purchaseGroup = findByIdWithLock(purchaseGroupId);
        // 공동 구매 참여 가능 여부 확인
        purchaseGroup.validatePurchaseGroupParticipation(orderQuantity);
        // 구매 진행 상황 업데이트
        purchaseGroup.updatePurchaseProgress(orderQuantity);
        // 업데이트된 그룹 저장
        purchaseGroupRepository.save(purchaseGroup);
        return purchaseGroup;
    }

    public void completePurchaseGroup(PurchaseGroup purchaseGroup) {
        // 비관적 락으로 해당 PurchaseGroup을 보호
        PurchaseGroup lockedGroup = findById(purchaseGroup.getId());

        if (lockedGroup.getCurrentPurchaseQuantity().equals(lockedGroup.getTargetPurchaseQuantity())) {
            lockedGroup.complete();  // 상태 변경
            purchaseGroupRepository.save(lockedGroup);
        }
    }

    // 공동 구매 그룹 찾기
    private PurchaseGroup findByIdWithLock(Long purchaseGroupId) {
        return purchaseGroupRepository.findByIdWithPessimisticLock(purchaseGroupId)
            .orElseThrow(() -> new IllegalArgumentException("공동 구매 그룹이 존재하지 않습니다."));
    }

    // 공동 구매 그룹 찾기
    private PurchaseGroup findById(Long purchaseGroupId) {
        return purchaseGroupRepository.findById(purchaseGroupId)
            .orElseThrow(() -> new IllegalArgumentException("공동 구매 그룹이 존재하지 않습니다."));
    }
}
