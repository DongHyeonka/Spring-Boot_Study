package com.springboot.rabitmq.handler;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(
        ServerHttpRequest request, 
        WebSocketHandler wsHandler, 
        Map<String, Object> attributes
    ) {
        try {
            // 1. 헤더에서 토큰 추출
            String token = extractToken(request);
            
            // 2. 토큰 검증 및 사용자 정보 추출
            //Authentication auth = jwtTokenService.getAuthentication(token);
            Authentication auth = null;
            
            // 3. 추가 속성 설정 (필요한 경우)
            attributes.put("username", auth.getName());
            
            log.info("[WebSocket] Handshake 성공 - User: {}", auth.getName());
            return auth;
            
        } catch (Exception e) {
            log.error("[WebSocket] Handshake 실패: {}", e.getMessage());
            throw new IllegalStateException("인증 실패", e);
        }
    }

    private String extractToken(ServerHttpRequest request) {
        List<String> authorization = request.getHeaders().get("Authorization");
        if (authorization == null || authorization.isEmpty()) {
            throw new IllegalArgumentException("Authorization 헤더가 없습니다.");
        }
        
        String token = authorization.get(0);
        if (!token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("잘못된 토큰 형식입니다.");
        }
        
        return token.substring(7);
    }
}
