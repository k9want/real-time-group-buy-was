package com.realtimegroupbuy.rtgb.configuration.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitListenerConfig {

    private final MessageConverter messageConverter;
    private final GlobalRabbitErrorHandler globalRabbitErrorHandler;

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
        ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setErrorHandler(globalRabbitErrorHandler);
        factory.setDefaultRequeueRejected(false); // 기본적으로 메시지를 재시도하지 않음
        return factory;
    }
}