package com.springboot.websocket_stomp.listener;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.springboot.websocket_stomp.event.MessageReadEvent;
import com.springboot.websocket_stomp.service.ChatService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatEventListener {
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // 읽음 상태 업데이트
    @TransactionalEventListener
    public void handleMessageRead(MessageReadEvent event) {
        // 메시지 읽음 상태 업데이트 로직 (DB 업데이트 등)
        // 읽음 상태를 해당 사용자에게 알림
    }
}
