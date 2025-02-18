package com.springboot.rabitmq.interceptor;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;

import com.springboot.rabitmq.settings.RabbitMQProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CustomMessageRecoverer implements MessageRecoverer {
    private static final String LOG_PREFIX = "[RabbitMQ-Retry]";
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties properties;

    @Override
    public void recover(Message message, Throwable cause) {
        try {
            String messageId = message.getMessageProperties().getMessageId();

            log.warn("{} 최대 재시도 횟수 초과 - 메시지 ID: {}, 예외: {}", 
                LOG_PREFIX, messageId, cause.getMessage());
            
            CorrelationData correlationData = new CorrelationData(messageId);
            rabbitTemplate.send(
                properties.getDeadLetter().getExchange(),
                properties.getDeadLetter().getRoutingKey(),
                message,
                correlationData
            );
            
            log.info("{} 메시지를 DLQ로 전송 완료 - 메시지 ID: {}", LOG_PREFIX, messageId);
        } catch (Exception e) {
            log.error("{} DLQ 전송 중 오류 발생", LOG_PREFIX, e);
        }
    }
    
}
