package com.realtimegroupbuy.rtgb.service.purchasegroup;

import com.realtimegroupbuy.rtgb.configuration.rabbitmq.RabbitMQConfig;
import com.realtimegroupbuy.rtgb.messaging.PurchaseParticipationRequest;
import com.realtimegroupbuy.rtgb.model.User;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PurchaseGroupPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 사용자가 공동 구매에 참여하는 요청을 메시지 큐에 발행합니다.
     *
     * @param user             참여하는 사용자
     * @param purchaseGroupId  참여하려는 공동 구매 그룹의 ID
     * @param orderQuantity    주문 수량
     */
    public void participatePurchaseGroup(User user, Long purchaseGroupId, Integer orderQuantity) {
        PurchaseParticipationRequest request = PurchaseParticipationRequest.builder()
            .requestId(UUID.randomUUID())  // 고유한 UUID 생성
            .userId(user.getId())
            .purchaseGroupId(purchaseGroupId)
            .orderQuantity(orderQuantity)
            .build();

        // 메시지 발행 (Exchange와 Routing Key를 사용)
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.PURCHASE_EXCHANGE,
            RabbitMQConfig.PURCHASE_PARTICIPATION_QUEUE,
            request);
    }
}
