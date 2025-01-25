package com.springboot.websocket_stomp.listener;

import java.security.Principal;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.springboot.websocket_stomp.service.ChatService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener implements ApplicationListener<SessionConnectEvent> {
    private final SimpUserRegistry simpUserRegistry;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @Override
    public void onApplicationEvent(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        Principal principal = headerAccessor.getUser();

        if (principal != null) {
            String username = principal.getName();
            System.out.println("User connected: " + username + ", Session ID: " + sessionId);
            // 추가적인 연결 처리 로직 (예: 사용자 상태 업데이트)
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        Principal principal = headerAccessor.getUser();

        if (principal != null) {
            String username = principal.getName();
            System.out.println("User disconnected: " + username + ", Session ID: " + sessionId);

            // 사용자가 참여하고 있던 모든 방에서 나가기 처리
            // chatService.leaveAllRooms(username);
        }
    }
}