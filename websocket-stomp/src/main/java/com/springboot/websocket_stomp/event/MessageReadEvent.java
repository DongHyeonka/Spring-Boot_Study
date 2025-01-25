package com.springboot.websocket_stomp.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageReadEvent {
    private String messageId;
    private String reader;
}
