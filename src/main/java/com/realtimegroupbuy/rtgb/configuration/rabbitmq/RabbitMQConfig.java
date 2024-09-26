package com.realtimegroupbuy.rtgb.configuration.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 교환기(Exchange) 이름
    public static final String PURCHASE_EXCHANGE = "purchase.exchange";

    // 큐(Queue) 이름
    public static final String PURCHASE_PARTICIPATION_QUEUE = "purchase.participation.queue";
    public static final String PURCHASE_SUCCESS_QUEUE = "purchase.success.queue";

    // 교환기 빈 등록 (Direct Exchange 사용)
    @Bean
    public DirectExchange purchaseExchange() {
        return new DirectExchange(PURCHASE_EXCHANGE);
    }

    // 공동 구매 참여 큐 빈 등록
    @Bean
    public Queue purchaseParticipationQueue() {
        return QueueBuilder.durable(PURCHASE_PARTICIPATION_QUEUE).build();
    }

    // 공동 구매 성공 큐 빈 등록
    @Bean
    public Queue purchaseSuccessQueue() {
        return QueueBuilder.durable(PURCHASE_SUCCESS_QUEUE).build();
    }

    // 공동 구매 참여 큐와 교환기 바인딩 (라우팅 키 동일하게 설정)
    @Bean
    public Binding purchaseParticipationBinding() {
        return BindingBuilder.bind(purchaseParticipationQueue())
            .to(purchaseExchange())
            .with(PURCHASE_PARTICIPATION_QUEUE);
    }

    // 공동 구매 성공 큐와 교환기 바인딩 (라우팅 키 동일하게 설정)
    @Bean
    public Binding purchaseSuccessBinding() {
        return BindingBuilder.bind(purchaseSuccessQueue())
            .to(purchaseExchange())
            .with(PURCHASE_SUCCESS_QUEUE);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
        ConnectionFactory connectionFactory,
        MessageConverter messageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

}
