package com.springboot.rabitmq.publisher;

import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.springboot.rabitmq.config.RabbitMQConfig;
import com.springboot.rabitmq.dto.ChatMessage;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessagePublisher {
    private final RabbitTemplate rabbitTemplate;

    public void sendPersistentMessage(ChatMessage message) {
        // RabbitTemplate의 convertAndSend 메서드를 사용하면,
        // 내부에 설정된 Jackson2JsonMessageConverter에 의해 객체가 JSON으로 자동 직렬화 됨
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CHAT_EXCHANGE,
                "chat.room.general", // 라우팅 키 - Exchange가 이 키에 따라 어떤 큐로 메시지를 보낼지 결정합니다. 일반 큐는 단체 채팅임임 만약 개인 채팅으로 보내고 싶다 그러면 개인 큐로 키를 설정해서 보내면 됨
                message,
                m -> {
                    m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    m.getMessageProperties().setContentType("application/json");
                    return m;
                }
        );
    }
}
