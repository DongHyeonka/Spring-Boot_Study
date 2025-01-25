package com.springboot.websocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

// 인터셉터와 리소스 서버의 필요성
/**
 * 리소스 서버의 경우 모든 메세지 수준으로 보안이 적용, 지속적인 메시지 검증, 연결 + 모든 메시지 보호, 세분화된 권한 관리
 * 인터셉터의 경우 연결 수립 및 종료 시 보안 적용, 연결 수립 및 종료 시 보호, 연결 수립 및 종료 시 보호, 연결 수립 및 종료 시 보호
 * 1. 연결 수립 시 초기 인증(토큰 검증) 인터셉터 필수
 * 2. 지속적인 메시지 검증 권한 관리 보안 리소스 서버 필수
 * 3. 전역 보안 정책 메서드 수준 접근 제어 필수
 */
@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig extends WebSecurityConfiguration {
    @Bean
    SecurityFilterChain webSocketSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .httpBasic(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable)
            .rememberMe(AbstractHttpConfigurer::disable)
            .requestCache(RequestCacheConfigurer::disable)
            .sessionManagement(sessions -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .securityContext(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
