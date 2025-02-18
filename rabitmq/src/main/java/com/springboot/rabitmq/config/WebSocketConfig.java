package com.springboot.rabitmq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.HandshakeHandler;

import com.springboot.rabitmq.handler.CustomAuthChannelInterceptor;
import com.springboot.rabitmq.handler.CustomHandshakeHandler;
import com.springboot.rabitmq.service.domain.MessageEncryptionService;
import com.springboot.rabitmq.service.domain.MessageSanitizer;
import com.springboot.rabitmq.settings.WebSocketProperties;
import com.springboot.rabitmq.utils.WebSocketSessionRegistry;

import lombok.RequiredArgsConstructor;

/**
 * 웹 소켓 설정
 * 인증 정보 하드코딩 되어있음
 * 에러 헨들링 부재
 * WebSocket 세션 관리 전략 부재
 */

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final WebSocketProperties webSocketProperties;
    private final CustomAuthChannelInterceptor customAuthChannelInterceptor;


    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(customAuthChannelInterceptor); // 의 존성 주입 사이클 주의
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(webSocketProperties.getEndpoint())
                .setAllowedOrigins(webSocketProperties.getAllowedOrigins())
                .setHandshakeHandler(customHandshakeHandler())
                .withSockJS()
                .setHeartbeatTime(webSocketProperties.getHeartbeatTime())
                .setDisconnectDelay(webSocketProperties.getDisconnectDelay());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes(webSocketProperties.getDestination().getApplicationPrefix());
        registry.enableStompBrokerRelay(webSocketProperties.getDestination().getBrokerPrefixes())
                .setRelayHost(webSocketProperties.getRelay().getHost())
                .setRelayPort(webSocketProperties.getRelay().getPort())
                .setClientLogin(webSocketProperties.getRelay().getClient().getLogin())
                .setClientPasscode(webSocketProperties.getRelay().getClient().getPasscode())
                .setSystemLogin(webSocketProperties.getRelay().getSystem().getLogin()) // 시스템 인증 설정
                .setSystemPasscode(webSocketProperties.getRelay().getSystem().getPasscode()) // STOMP 브로커 릴레이가 RabittMQ와 연결할 때 사용하는 시스템 레벨의 인증 정보 일반 클라와는 별도로 관리되어서 높은 권한을 가질 수 있음
                .setSystemHeartbeatSendInterval(webSocketProperties.getRelay().getSystem().getHeartbeat().getSendInterval()) // 클라이언트가 서버에 대해 주기적으로 5초마다 하트비트 메시지를 보냄
                .setSystemHeartbeatReceiveInterval(webSocketProperties.getRelay().getSystem().getHeartbeat().getReceiveInterval()); // 서버가 클라이언트에 대해 주기적으로 4초 간격으로 하트비트 수신을 기대함
        registry.setUserDestinationPrefix(webSocketProperties.getDestination().getUserPrefix());
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(webSocketProperties.getTransport().getMessageSizeLimit()) // 64KB 메시지 크기 제한
                    .setSendTimeLimit(webSocketProperties.getTransport().getSendTimeLimit()) // 10초 전송 시간 제한
                    .setSendBufferSizeLimit(webSocketProperties.getTransport().getSendBufferSizeLimit()); // 10MB 전송 버퍼 크기 제한
    }

    @Bean
    public HandshakeHandler customHandshakeHandler() {
        return new CustomHandshakeHandler();
    }

    @Bean
    public ChannelInterceptor customAuthChannelInterceptor(MessageEncryptionService encryptionService, MessageSanitizer messageSanitizer, WebSocketSessionRegistry sessionRegistry) {
        return new CustomAuthChannelInterceptor(encryptionService, messageSanitizer, sessionRegistry);
    }
}
