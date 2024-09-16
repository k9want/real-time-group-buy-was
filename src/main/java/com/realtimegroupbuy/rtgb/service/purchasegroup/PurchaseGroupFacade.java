package com.realtimegroupbuy.rtgb.service.purchasegroup;

import com.realtimegroupbuy.rtgb.model.Order;
import com.realtimegroupbuy.rtgb.model.PurchaseGroup;
import com.realtimegroupbuy.rtgb.model.User;
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
        // 1.공동 구매 참여 (구매 참여 가능 여부 및 수량 업데이트)
        PurchaseGroup purchaseGroup = purchaseGroupService.participateInPurchaseGroup(
            purchaseGroupId, orderQuantity);

        // 2.주문 생성 및 결제 완료 처리
        Order order = orderService.create(user, purchaseGroup, orderQuantity);
        order = orderService.completePayment(order);

        // 3. 공동 구매 완료 여부 확인 및 상태 업데이트
        if (!purchaseGroup.isCompleted() && purchaseGroup.getCurrentPurchaseQuantity().equals(purchaseGroup.getTargetPurchaseQuantity())) {
            purchaseGroupService.completePurchaseGroup(purchaseGroup);
        }

//        // 3.공동 구매 완료 여부 확인 및 상태 업데이트
//        orderService.checkPurchaseComplete(purchaseGroup);

        return order;
    }
}
