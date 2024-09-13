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
            .currentPurchaseQuantityCount(DEFAULT__CURRENT_PURCHASE_QUANTITY)
            .expiresAt(expiredAt)
            .status(PurchaseGroupStatus.IN_PROGRESS)
            .build();

        return purchaseGroupRepository.save(purchaseGroup);
    }

    // 모든 공동 구매 그룹을 페이지네이션으로 조회
    @Transactional(readOnly = true)
    public Page<PurchaseGroup> getAllPurchaseGroups(Pageable pageable) {
        return purchaseGroupRepository.findAll(pageable);
    }

    @Transactional
    public void updatePurchaseGroupParticipation(PurchaseGroup purchaseGroup, Integer orderQuantity) {
        // 공동 구매 그룹에 구매 수량 업데이트 요청
        purchaseGroup.updatePurchaseQuantity(orderQuantity);
        purchaseGroupRepository.save(purchaseGroup); // 변경된 상태를 저장
    }

    public PurchaseGroup findById(Long purchaseGroupId) {
        return purchaseGroupRepository.findById(purchaseGroupId)
            .orElseThrow(() -> new IllegalArgumentException("공동구매 그룹이 존재하지 않습니다."));
    }


}
