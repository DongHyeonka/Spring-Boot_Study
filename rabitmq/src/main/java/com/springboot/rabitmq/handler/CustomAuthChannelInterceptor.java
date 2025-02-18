package com.springboot.rabitmq.handler;

import java.nio.charset.StandardCharsets;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketSession;

import com.springboot.rabitmq.service.domain.MessageEncryptionService;
import com.springboot.rabitmq.service.domain.MessageSanitizer;
import com.springboot.rabitmq.utils.WebSocketSessionRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CustomAuthChannelInterceptor implements ChannelInterceptor {
    // private final JwtTokenService jwtTokenService;
    private final MessageEncryptionService encryptionService;
    private final MessageSanitizer messageSanitizer;
    private final WebSocketSessionRegistry sessionRegistry;

    private static final int MAX_MESSAGE_LENGTH = 1000;
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
            message, StompHeaderAccessor.class);

        if (accessor != null) {
            switch (accessor.getCommand()) {
                case CONNECT:
                    return handleConnect(accessor, message);
                case SUBSCRIBE:
                    return handleSubscribe(accessor, message);
                case SEND:
                    return handleSend(accessor, message);
                case DISCONNECT:
                    return handleDisconnect(accessor, message);
                default:
                    return message;
            }
        }
        return message;
    }

    private Message<?> handleDisconnect(StompHeaderAccessor accessor, Message<?> message) {
        sessionRegistry.closeSession(accessor.getSessionId());
        return message;
    }

    private Message<?> handleConnect(StompHeaderAccessor accessor, Message<?> message) {
        try {
            validateAuthentication();
            validateToken(accessor);
            setUserInformation(accessor);
            sessionRegistry.registerSession(accessor.getSessionId(), (WebSocketSession) accessor.getHeader("simpSessionAttributes"));
            return message;
        } catch (Exception e) {
            log.error("[WebSocket] 연결 실패: {}", e.getMessage());
            throw new MessageDeliveryException("Authentication failed: " + e.getMessage());
        }
    }

    private Message<?> handleSubscribe(StompHeaderAccessor accessor, Message<?> message) {
        try {
            String destination = accessor.getDestination();
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (!canSubscribe(auth, destination)) {
                throw new MessageDeliveryException("구독 권한이 없습니다: " + destination);
            }
            
            log.info("[WebSocket] 구독 성공 - User: {}, Destination: {}", 
                auth.getName(), destination);
            return message;
        } catch (Exception e) {
            log.error("[WebSocket] 구독 실패: {}", e.getMessage());
            throw new MessageDeliveryException("Subscription failed: " + e.getMessage());
        }
    }

    private Message<?> handleSend(StompHeaderAccessor accessor, Message<?> message) {
        try {
            processSendMessage(message, accessor);
            String destination = accessor.getDestination();
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (!canSend(auth, destination)) {
                throw new MessageDeliveryException("메시지 전송 권한이 없습니다: " + destination);
            }
            
            log.info("[WebSocket] 메시지 전송 - User: {}, Destination: {}", 
                auth.getName(), destination);
            return message;
        } catch (Exception e) {
            log.error("[WebSocket] 메시지 전송 실패: {}", e.getMessage());
            throw new MessageDeliveryException("Message sending failed: " + e.getMessage());
        }
    }

    private Message<?> processSendMessage(Message<?> message, StompHeaderAccessor accessor) {
        try {
            // 1. 메시지 페이로드 추출
            String payload = new String((byte[]) message.getPayload(), StandardCharsets.UTF_8);
            
            // 2. 입력값 검증
            validateMessageContent(payload);
            
            // 3. XSS 방지를 위한 sanitize
            String sanitizedPayload = messageSanitizer.sanitize(payload);
            
            // 4. 메시지 암호화
            String encryptedPayload = encryptionService.encrypt(sanitizedPayload);
            
            // 5. 새로운 메시지 생성
            return MessageBuilder
                .createMessage(
                    encryptedPayload.getBytes(StandardCharsets.UTF_8),
                    accessor.getMessageHeaders()
                );
        } catch (Exception e) {
            log.error("[WebSocket] 메시지 처리 실패: {}", e.getMessage());
            throw new MessageDeliveryException("Message processing failed: " + e.getMessage());
        }
    }

    private void validateMessageContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new MessageDeliveryException("메시지 내용이 비어있습니다.");
        }
        
        if (content.length() > MAX_MESSAGE_LENGTH) {
            throw new MessageDeliveryException("메시지가 너무 깁니다. 최대 길이: " + MAX_MESSAGE_LENGTH);
        }
        
        // 위험한 패턴 검사
        if (containsMaliciousContent(content)) {
            throw new MessageDeliveryException("잠재적으로 위험한 콘텐츠가 포함되어 있습니다.");
        }
    }

    private boolean containsMaliciousContent(String content) {
        // SQL 인젝션 패턴
        if (content.toLowerCase().contains("select") || 
            content.toLowerCase().contains("union") ||
            content.toLowerCase().contains("delete")) {
            return true;
        }
        
        // 스크립트 태그
        if (content.contains("<script") || 
            content.contains("javascript:")) {
            return true;
        }
        
        return false;
    }

    private String extractAndValidateToken(StompHeaderAccessor accessor) {
        String token = accessor.getFirstNativeHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new MessageDeliveryException("유효하지 않은 토큰 형식입니다.");
        }
        
        String jwt = token.substring(7);
        // if (!jwtTokenService.validateToken(jwt)) {
        //     throw new MessageDeliveryException("유효하지 않은 토큰입니다.");
        // }
        
        return jwt;
    }

    private boolean canSubscribe(Authentication auth, String destination) {
        // 구독 권한 검사 로직
        // 예: /topic/public/** - 모든 사용자 허용
        // 예: /user/queue/** - 인증된 사용자만 허용
        // 예: /topic/admin/** - ADMIN 역할만 허용
        if (destination.startsWith("/topic/public/")) {
            return true;
        }
        if (destination.startsWith("/user/queue/")) {
            return auth != null && auth.isAuthenticated();
        }
        if (destination.startsWith("/topic/admin/")) {
            return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }
        return false;
    }

    private boolean canSend(Authentication auth, String destination) {
        // 메시지 전송 권한 검사 로직
        // 구독과 유사한 로직 적용
        return canSubscribe(auth, destination);
    }

    private void validateAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.error("[WebSocket] 인증되지 않은 연결 시도");
            throw new MessageDeliveryException("Unauthorized connection attempt");
        }
    }

    private void validateToken(StompHeaderAccessor accessor) {
        String token = accessor.getFirstNativeHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            //jwtTokenService.validateToken(jwt);
        }
    }

    private void setUserInformation(StompHeaderAccessor accessor) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        accessor.setUser(auth);
        log.info("[WebSocket] 연결 성공 - User: {}", auth.getName());
    }
}
