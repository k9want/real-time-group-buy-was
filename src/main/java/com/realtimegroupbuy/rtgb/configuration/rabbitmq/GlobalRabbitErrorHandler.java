package com.realtimegroupbuy.rtgb.configuration.rabbitmq;

import com.realtimegroupbuy.rtgb.exception.PurchaseGroupFullException;
import com.realtimegroupbuy.rtgb.exception.PurchaseGroupStatusNotInProgressException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

@Slf4j
@Component
public class GlobalRabbitErrorHandler implements ErrorHandler {

    @Override
    public void handleError(Throwable t) {
        // 예외가 ListenerExecutionFailedException인 경우
        if (t instanceof ListenerExecutionFailedException) {
            Throwable cause = t.getCause();
            if (cause instanceof PurchaseGroupStatusNotInProgressException ||
                cause instanceof PurchaseGroupFullException) {
                // 비즈니스 예외 발생 시, INFO 레벨로 로그 기록하고 예외를 던지지 않음
                log.info("비즈니스 예외 발생: {}", cause.getMessage());
                // 메시지를 재시도하지 않도록 예외 던지기
                return;
            }
        }
        // 그 외의 예외는 기본적으로 처리 (재시도 가능)
        log.error("메시지 리스너 처리 중 예외 발생", t);
        throw new RuntimeException(t);
    }
}