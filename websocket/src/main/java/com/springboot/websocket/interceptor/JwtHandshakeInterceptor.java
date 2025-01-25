package com.springboot.websocket.interceptor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.springboot.websocket.service.JwtTokenService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
    private final JwtTokenService jwtTokenService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {
        String token = extractJwtToken(request); // JWT 토큰 추출

        if (token != null && jwtTokenService.verifyAndGetClaims(token) != null) { // 토큰 검증
            Authentication authentication = jwtTokenService.getAuthentication(token); // Authentication 객체 생성
            attributes.put("authentication", authentication); // WebSocketSession에 인증 정보 저장
            return true;
        } else {
            response.setStatusCode(HttpStatus.UNAUTHORIZED); // 401 Unauthorized 응답 설정
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Exception exception) {
        // TODO : 딱히 구현할 거 없음.
    }

    private String extractJwtToken(ServerHttpRequest request) {
        String authorizationHeader = request.getHeaders().getFirst("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
