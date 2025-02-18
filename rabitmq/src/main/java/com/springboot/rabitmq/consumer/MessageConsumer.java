package com.springboot.rabitmq.consumer;

import java.io.IOException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.rabbitmq.client.Channel;
import com.springboot.rabitmq.config.RabbitMQConfig;
import com.springboot.rabitmq.dto.ChatMessage;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MessageConsumer {
    private final SimpMessagingTemplate simpMessagingTemplate;

    /**
     * 단체(그룹) 채팅 메시지 처리 : 일반 큐 사용용
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitListener(queues = "#{rabbitMQProperties.generalQueueName}")
    public void handleMessage(Message message, Channel channel) throws IOException {
        try {
            // 메시지 처리 로직
            ChatMessage chatMessage = (ChatMessage) RabbitMQConfig.jsonMessageConverter().fromMessage(message);

            // roomId가 있으면 해당 채팅방, 없으면 기본 general 채팅방 경로 사용
            String roomId = StringUtils.hasText(chatMessage.getRoomId()) ? chatMessage.getRoomId() : "general";
            String destination = "/topic/chatroom/" + roomId;

            simpMessagingTemplate.convertAndSend(destination, chatMessage);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); // 수동 ACK
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true); // 재시도
        }
    }

    /**
     * 개인 1대 1 채팅 메시지 처리 : 개인 큐 사용
     */
    @RabbitListener(queues = "${rabbitmq.general-queue-name}")
    public void handlePrivateChatMessage(Message message, Channel channel) throws IOException {
        try {
            // 개인 채팅 메시지 역직렬화
            ChatMessage chatMessage = (ChatMessage) RabbitMQConfig.jsonMessageConverter().fromMessage(message);
            
            // targetUserId가 존재해야 개인 메시지로 판단
            if (StringUtils.hasText(chatMessage.getTargetUserId())) {
                // STOMP 전송: 보통 /user/{targetUserId}/queue/messages 경로 사용
                simpMessagingTemplate.convertAndSendToUser(
                    chatMessage.getTargetUserId(),
                    "/queue/messages",
                    chatMessage
                );
            }
            // 처리 성공 시 ACK 전송
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 처리 실패 시 NACK 전송하여 재시도 처리
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }
}
