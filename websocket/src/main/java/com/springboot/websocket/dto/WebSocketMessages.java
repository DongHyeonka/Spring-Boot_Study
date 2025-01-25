package com.springboot.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebSocketMessages {
    public enum MessageType { ERROR, ACK, BROADCAST, TEXT, BINARY }

    private MessageType type;
    private String code;
    private String message;
    private String data;
    private String sessionId;
}
