package com.springboot.rabitmq.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "rabbitmq")
@Configuration
public class RabbitMQProperties {
    // Exchange 설정
    @NotNull
    private String exchangeName = "chat.topic";
    
    // Queue 설정
    @NotNull
    private String generalQueueName = "chat.general";
    private String privateQueueName = "chat.private";

    // Dead Letter Exchange 설정
    private final DeadLetter deadLetter = new DeadLetter();

    // Routing Key 설정
    private final RoutingKey routingKey = new RoutingKey();
    
    // Consumer 설정
    @Min(1)
    private int prefetchCount = 250;
    @Min(1)
    private int concurrentConsumers = 3;
    @Min(1)
    private int maxConcurrentConsumers = 10;
    
    // Producer 설정
    private long replyTimeout = 5000;
    private long receiveTimeout = 5000;
    private int batchSize = 100;
    private int bufferLimit = 10000;

    // 메시지 TTL 설정
    private long messageTtl = 24 * 60 * 60 * 1000; // 24시간

    @Getter
    @Setter
    public static class DeadLetter {
        private String exchange = "chat.dlx";
        private String queue = "chat.dead";
        private String routingKey = "chat.dead";
    }

    @Getter
    @Setter
    public static class RoutingKey {
        private String generalPattern = "chat.room.*";    // 일반 채팅방 라우팅 패턴
        private String privatePattern = "chat.user.#";    // 개인 채팅 라우팅 패턴
    }
}
