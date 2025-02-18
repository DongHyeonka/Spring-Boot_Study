package com.springboot.rabitmq.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "websocket")
@Validated
@Configuration
public class WebSocketProperties {
    @NotNull
    private String endpoint;

    @NotNull
    private String[] allowedOrigins;

    private int heartbeatTime = 25000;

    private int disconnectDelay = 10000;

    @Valid
    private final Relay relay = new Relay();

    @Valid
    private final Transport transport = new Transport();

    @Valid
    private final Destination destination = new Destination();

    @Getter
    @Setter
    public static class Relay {
        private String host = "localhost";
        private int port = 61613;
        
        @Valid
        private final Client client = new Client();
        
        @Valid
        private final System system = new System();

        @Getter
        @Setter
        public static class Client {
            private String login = "guest";
            private String passcode = "guest";
        }

        @Getter
        @Setter
        public static class System {
            private String login = "guest";
            private String passcode = "guest";
            
            @Valid
            private final Heartbeat heartbeat = new Heartbeat();

            @Getter
            @Setter
            public static class Heartbeat {
                private int sendInterval = 5000;
                private int receiveInterval = 4000;
            }
        }
    }

    @Getter
    @Setter
    public static class Transport {
        private int messageSizeLimit = 64 * 1024;        // 64KB
        private int sendTimeLimit = 10000;               // 10초
        private int sendBufferSizeLimit = 10 * 1024 * 1024; // 10MB
    }

    @Getter
    @Setter
    public static class Destination {
        private String applicationPrefix = "/app";
        private String userPrefix = "/user";
        private String[] brokerPrefixes = {"/topic", "/queue"};
    }
}
