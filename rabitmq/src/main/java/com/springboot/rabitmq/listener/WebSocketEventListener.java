package com.springboot.rabitmq.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.springboot.rabitmq.dto.ChatMessage;
import com.springboot.rabitmq.service.domain.ChatRoomService;

import lombok.RequiredArgsConstructor;

/**
 * 웹소켓 핸드셰이크 인터셉터를 이용하여 접속 시 사용자 이름 등의 정보를 세션에 저장할 수 있습니다.
 */
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomService chatRoomService;

    // 연결 시도 시점
    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        logger.info("[WebSocket] 연결 시도 - Session ID: {}", accessor.getSessionId());
    }

    @EventListener
    public void handleWebSocketConnectionListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        
        logger.info("[WebSocket] 새로운 연결 - Session ID: {}, Username: {}", sessionId, username);
        
        if (username != null) {
            //chatRoomService.handleUserConnection(username, sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");


        if (username != null) {
            logger.info("[WebSocket] 연결 종료 - Session ID: {}, Username: {}, Room: {}", 
                     sessionId, username, roomId);
            
            // 사용자 상태 업데이트
            //chatRoomService.handleUserDisconnection(username, sessionId);

            // 연결 해제 시 알림 메시지 전송
            ChatMessage leaveMessage = ChatMessage.builder()
                    .type(ChatMessage.MessageType.LEAVE)
                    .sender(username)
                    .roomId(roomId)
                    .timestamp(System.currentTimeMillis())
                    .build();
            if (roomId != null) {
                messagingTemplate.convertAndSend("/topic/room/" + roomId, leaveMessage);
                //chatRoomService.removeUserFromRoom(roomId, username);
            }
        }
    }
}
