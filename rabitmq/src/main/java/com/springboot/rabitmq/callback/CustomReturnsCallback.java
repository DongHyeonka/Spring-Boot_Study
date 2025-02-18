package com.springboot.rabitmq.callback;

import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnsCallback;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomReturnsCallback implements ReturnsCallback {
    private static final String LOG_PREFIX = "[RabbitMQ-Return]";

    @Override
    public void returnedMessage(ReturnedMessage returned) {
        log.error("{} 메시지 반환됨 - Exchange: {}, RoutingKey: {}, ReplyCode: {}, ReplyText: {}", 
            LOG_PREFIX,
            returned.getExchange(),
            returned.getRoutingKey(),
            returned.getReplyCode(),
            returned.getReplyText()
        );
    }
}
