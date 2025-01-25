package com.springboot.websocket.handler;

import org.springframework.security.core.Authentication;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

import com.springboot.websocket.dto.WebSocketMessages;

public class AuthenticationWebSocketHandler extends WebSocketHandlerDecorator{

    public AuthenticationWebSocketHandler(WebSocketHandler delegate) {
        super(delegate);
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Authentication authentication = (Authentication) session.getAttributes().get("authentication");

        if (authentication != null && authentication.isAuthenticated()) {
            // 인증된 사용자 처리를 위해 부모 클래스의 메서드 호출
            super.afterConnectionEstablished(session);
        } else {
            // 인증되지 않은 사용자에 대한 처리
            sendAuthenticationError(session, "AUTHENTICATION_REQUIRED", "WebSocket 연결을 위해 인증이 필요합니다.");
            session.close(CloseStatus.POLICY_VIOLATION);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        Authentication authentication = (Authentication) session.getAttributes().get("authentication");

        if (authentication != null && authentication.isAuthenticated()) {
            // 인증된 사용자의 메시지 처리를 위해 부모 클래스의 메서드 호출
            super.handleMessage(session, message);
        } else {
            // 인증되지 않은 사용자에 대한 처리
            sendAuthenticationError(session, "UNAUTHORIZED", "메시지 처리를 위해 인증이 필요합니다.");
        }
    }

    private void sendAuthenticationError(WebSocketSession session, String errorCode, String errorMessage) throws Exception {
        WebSocketMessages errorMsg = WebSocketMessages.builder()
                .type(WebSocketMessages.MessageType.ERROR)
                .code(errorCode)
                .message(errorMessage)
                .sessionId(session.getId())
                .build();

        session.sendMessage(new TextMessage(convertWebSocketMessageToJson(errorMsg)));
    }

    private String convertWebSocketMessageToJson(WebSocketMessages message) {
        return "";
    }
}
