package com.realtimegroupbuy.rtgb.service.purchasegroup;

import com.realtimegroupbuy.rtgb.configuration.rabbitmq.RabbitMQConfig;
import com.realtimegroupbuy.rtgb.exception.PurchaseGroupFullException;
import com.realtimegroupbuy.rtgb.messaging.PurchaseParticipationRequest;
import com.realtimegroupbuy.rtgb.messaging.PurchaseSuccessRequest;
import com.realtimegroupbuy.rtgb.model.Order;
import com.realtimegroupbuy.rtgb.model.PurchaseGroup;
import com.realtimegroupbuy.rtgb.model.PurchaseParticipationLog;
import com.realtimegroupbuy.rtgb.model.User;
import com.realtimegroupbuy.rtgb.exception.PurchaseGroupStatusNotInProgressException;
import com.realtimegroupbuy.rtgb.repository.PurchaseParticipationLogRepository;
import com.realtimegroupbuy.rtgb.repository.UserRepository;
import com.realtimegroupbuy.rtgb.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PurchaseParticipationConsumer {

    private final PurchaseGroupService purchaseGroupService;
    private final OrderService orderService;
    private final UserRepository userRepository;
    private final PurchaseParticipationLogRepository logRepository;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 공동 구매 참여 요청 메시지를 소비하여 처리합니다.
     *
     * @param request 메시지 객체
     */
    @RabbitListener(queues = RabbitMQConfig.PURCHASE_PARTICIPATION_QUEUE)
    @Transactional
    public void handlePurchaseParticipation(PurchaseParticipationRequest request) {
        // 멱등성 체크: 이미 처리된 요청인지 확인
        if (logRepository.existsByRequestId(request.getRequestId())) {
            // 이미 처리된 요청이므로 무시
            return;
        }

        try {
            // 사용자 조회
            User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

            // 공동 구매 참여: 구매 수량 업데이트
            PurchaseGroup purchaseGroup = purchaseGroupService.participateInPurchaseGroup(
                request.getPurchaseGroupId(), request.getOrderQuantity());

            // 주문 생성
            Order order = orderService.create(user, purchaseGroup, request.getOrderQuantity());

            // 결제 완료
            orderService.completePayment(order);

            // 로그 저장하여 멱등성 보장
            PurchaseParticipationLog log = PurchaseParticipationLog.builder()
                .requestId(request.getRequestId())
                .userId(user.getId())
                .purchaseGroupId(purchaseGroup.getId())
                .orderId(order.getId())
                .timestamp(java.time.LocalDateTime.now())
                .build();

            logRepository.save(log);

            // 구매 수량이 목표에 도달했는지 확인
            if (purchaseGroup.getCurrentPurchaseQuantity().equals(purchaseGroup.getTargetPurchaseQuantity())) {
                // 수량 충족 시, 성공 메시지 발행
                PurchaseSuccessRequest successRequest = PurchaseSuccessRequest.builder()
                    .purchaseGroupId(purchaseGroup.getId())
                    .build();
                rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PURCHASE_EXCHANGE,
                    RabbitMQConfig.PURCHASE_SUCCESS_QUEUE,
                    successRequest);
            }

        } catch (PurchaseGroupStatusNotInProgressException | PurchaseGroupFullException e) {
            // 비즈니스 예외 발생 시, 메시지를 재시도하지 않도록 예외 던지기
            throw new AmqpRejectAndDontRequeueException("공동 구매 참여 처리 중 비즈니스 예외 발생", e);
        }
        catch (Exception e) {
            // 그 외의 예외는 재시도하도록 던지기
            throw new IllegalArgumentException(e);
        }
    }
}
