package com.springboot.websocket.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

import com.springboot.websocket.dto.WebSocketMessages;

public class RateLimitedWebSocketHandler extends WebSocketHandlerDecorator{
    private static final Logger logger = LoggerFactory.getLogger(RateLimitedWebSocketHandler.class);
    private final Map<String, AtomicInteger> sessionMessageCount = new ConcurrentHashMap<>();
    private final int maxMessagesPerSecond;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public RateLimitedWebSocketHandler(WebSocketHandler delegate, int maxMessagesPerSecond) {
        super(delegate);
        this.maxMessagesPerSecond = maxMessagesPerSecond;
        scheduleResetTask();
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (isRateLimited(session)) {
            sendError(session, "RATE_LIMITED", "전송 속도 제한을 초과했습니다.");
            return;
        }
        getDelegate().handleMessage(session, message);
    }

    private boolean isRateLimited(WebSocketSession session) {
        String sessionId = session.getId();
        AtomicInteger count = sessionMessageCount.computeIfAbsent(sessionId, k -> new AtomicInteger(0));
        return count.incrementAndGet() > maxMessagesPerSecond;
    }

    private void sendError(WebSocketSession session, String errorCode, String errorMessage) throws Exception {
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

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionMessageCount.remove(session.getId()); // 세션 종료 시 카운트 제거
        super.afterConnectionClosed(session, status);
    }
    
    private void scheduleResetTask() {
        scheduler.scheduleAtFixedRate(() -> {
            sessionMessageCount.values().forEach(count -> count.set(0));
        }, 1, 1, TimeUnit.SECONDS);
    }
}
