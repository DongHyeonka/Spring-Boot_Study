package com.springboot.rabitmq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSocketSecurityConfig{
    @Bean
    public SecurityFilterChain webSocketSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .httpBasic(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable)
            .rememberMe(AbstractHttpConfigurer::disable)
            .requestCache(RequestCacheConfigurer::disable)
            .sessionManagement(sessions -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .securityContext(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/ws/**").authenticated() // WebSocket 핸드셰이크 인증 요구 여기서 인증된 사용자는 연결을 시도 하려고 함. 이 시점에서 Http 시큐리티의 역할은 끝 그 뒤 웹 소켓 연결 설정에서 헨들러를 통해서 이어서 보안 설정 진행
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
