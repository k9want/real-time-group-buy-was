package com.realtimegroupbuy.rtgb.service.groupbuy;

import com.realtimegroupbuy.rtgb.model.GroupBuy;
import com.realtimegroupbuy.rtgb.model.Product;
import com.realtimegroupbuy.rtgb.model.User;
import com.realtimegroupbuy.rtgb.model.enums.GroupBuyStatus;
import com.realtimegroupbuy.rtgb.repository.GroupBuyRepository;
import com.realtimegroupbuy.rtgb.repository.ProductRepository;
import com.realtimegroupbuy.rtgb.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupBuyService {

    private final UserRepository userRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final ProductRepository productRepository;

    public GroupBuy create(User user, Long productId, Integer targetQuantity, LocalDateTime expiredAt) {
        // 도메인 (최소 주문 개수: 10개 이상, 최소 유효 기간: 1일)
        if (targetQuantity < 10) {
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

        // 제품 재고 파악
        if (product.getStock() < targetQuantity) {
            throw new IllegalArgumentException("현재 제품의 재고보다 많은 수량을 지정했습니다.");
        }

        // 제품 재고 차감 - 공동 구매 그룹을 위해 미리 재고 확보 (동시성 문제 발생 지점)
        product.setStock(product.getStock() - targetQuantity);
        productRepository.save(product);

        // 공동구매 그룹 생성
        GroupBuy groupBuy = GroupBuy.builder()
            .product(product)
            .creator(creator)
            .targetPurchaseQuantity(targetQuantity)
            .currentPurchaseQuantityCount(0)
            .expiresAt(expiredAt)
            .status(GroupBuyStatus.IN_PROGRESS)
            .build();

        return groupBuyRepository.save(groupBuy);
    }
}
