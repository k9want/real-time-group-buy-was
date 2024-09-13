package com.realtimegroupbuy.rtgb.service.purchasegroup;

import com.realtimegroupbuy.rtgb.model.Order;
import com.realtimegroupbuy.rtgb.model.PurchaseGroup;
import com.realtimegroupbuy.rtgb.model.User;
import com.realtimegroupbuy.rtgb.model.enums.PurchaseGroupStatus;
import com.realtimegroupbuy.rtgb.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PurchaseGroupFacade {

    private final PurchaseGroupService purchaseGroupService;
    private final OrderService orderService;

    @Transactional
    public Order participatePurchaseGroup(User user, Long purchaseGroupId, Integer orderQuantity) {
        // 1. 공동 구매 그룹 가져오기
        PurchaseGroup purchaseGroup = purchaseGroupService.findById(purchaseGroupId);

        // 2. 공동 구매 참여 가능 여부 확인
        purchaseGroup.validatePurchaseGroupParticipation(orderQuantity);

        // 3. 주문 생성
        Order order = orderService.create(user, purchaseGroup, orderQuantity);

        // 4. 결제 완료 처리 [현재는 항상 결제 완료로 처리]
        orderService.completePayment(order);

        // 5. 공동 구매 그룹 업데이트
        purchaseGroupService.updatePurchaseGroupParticipation(purchaseGroup, orderQuantity);

        // 6.공동 구매 완료 여부 확인 및 상태 업데이트
        if (purchaseGroup.getStatus() == PurchaseGroupStatus.COMPLETED) {
            orderService.updateOrdersToSuccess(purchaseGroup);
        }

        return order;
    }
}
