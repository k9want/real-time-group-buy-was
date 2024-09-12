package com.realtimegroupbuy.rtgb.service.order;

import com.realtimegroupbuy.rtgb.model.Order;
import com.realtimegroupbuy.rtgb.model.PurchaseGroup;
import com.realtimegroupbuy.rtgb.model.User;
import com.realtimegroupbuy.rtgb.model.enums.OrderStatus;
import com.realtimegroupbuy.rtgb.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order create(User user, PurchaseGroup purchaseGroup, Integer orderQuantity) {
        // 1. 주문 생성
        Order order = Order.builder()
            .user(user)
            .purchaseGroup(purchaseGroup)
            .quantity(orderQuantity)
            .status(OrderStatus.PENDING)
            .build();

        // 2. 주문 총 금액 계산
        order.calculateTotalAmount();

        // 3. 주문 저장
        return orderRepository.save(order);
    }

    @Transactional
    public void completePayment(Order order) {
        // 결제 완료 처리
        order.completePayment();
        orderRepository.save(order);
    }
}
