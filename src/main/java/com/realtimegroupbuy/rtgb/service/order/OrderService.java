package com.realtimegroupbuy.rtgb.service.order;

import com.realtimegroupbuy.rtgb.model.Order;
import com.realtimegroupbuy.rtgb.model.PurchaseGroup;
import com.realtimegroupbuy.rtgb.model.User;
import com.realtimegroupbuy.rtgb.model.enums.OrderStatus;
import com.realtimegroupbuy.rtgb.repository.OrderRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order create(User user, PurchaseGroup purchaseGroup, Integer orderQuantity) {
        Order order = Order.builder()
            .user(user)
            .purchaseGroup(purchaseGroup)
            .quantity(orderQuantity)
            .status(OrderStatus.PENDING)
            .build();

        return orderRepository.save(order);
    }

    @Transactional
    public Order completePayment(Order order) {
        // 결제 완료 처리
        Order completePayment = order.completePayment();
        return orderRepository.save(completePayment);
    }

    @Transactional
    public void checkPurchaseComplete(PurchaseGroup purchaseGroup) {
        if (purchaseGroup.isCompleted()) {
            updateOrdersToSuccess(purchaseGroup);
        }
    }

    @Transactional
    public List<Order> updateOrdersToSuccess(PurchaseGroup purchaseGroup) {
        // 해당 공동 구매 그룹의 모든 APPROVE 상태의 주문 가져오기
        List<Order> orders = orderRepository.findAllByPurchaseGroupAndStatus(purchaseGroup, OrderStatus.APPROVE);
        // 각 주문 상태를 SUCCESS로 변경
        orders.forEach(Order::success);
        // 저장
        return orderRepository.saveAll(orders);
    }
}
