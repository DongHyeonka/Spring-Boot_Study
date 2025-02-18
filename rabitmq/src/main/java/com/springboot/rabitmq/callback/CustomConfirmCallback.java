package com.springboot.rabitmq.callback;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomConfirmCallback implements ConfirmCallback {
    private static final String LOG_PREFIX = "[RabbitMQ-Confirm]";

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            log.debug("{} 메시지 전송 확인 - ID: {}", 
                LOG_PREFIX, 
                correlationData != null ? correlationData.getId() : "unknown"
            );
        } else {
            log.error("{} 메시지 전송 실패 - ID: {}, 원인: {}", 
                LOG_PREFIX, 
                correlationData != null ? correlationData.getId() : "unknown", 
                cause
            );
        }
    }
}
