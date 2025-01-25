package com.springboot.websocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.springboot.websocket.handler.AuthenticationWebSocketHandler;
import com.springboot.websocket.handler.CompressionLibraryWebSocketHandler;
import com.springboot.websocket.handler.CompressionMessageWebSocketHandler;
import com.springboot.websocket.handler.CustomWebSocketHandler;
import com.springboot.websocket.handler.RateLimitedWebSocketHandler;
import com.springboot.websocket.interceptor.JwtHandshakeInterceptor;
import com.springboot.websocket.utils.WebSocketMessageConverter;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final CustomWebSocketHandler webSocketHandler;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    public WebSocketConfig(CustomWebSocketHandler webSocketHandler, JwtHandshakeInterceptor jwtHandshakeInterceptor) {
        this.webSocketHandler = webSocketHandler;
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOrigins("*");
    }

    @Bean
    public WebSocketHandler webSocketHandler(WebSocketMessageConverter messageConverter) {
        return new AuthenticationWebSocketHandler(
            new RateLimitedWebSocketHandler(
                new CompressionMessageWebSocketHandler(
                    new CompressionLibraryWebSocketHandler(
                        new CustomWebSocketHandler(messageConverter)
                    )
                ), 10)
        );
    }
}
