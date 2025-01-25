package com.springboot.websocket.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.websocket.dto.WebSocketMessages;
import com.springboot.websocket.exception.MessageConversionException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketMessageConverter {
    private final ObjectMapper objectMapper;

    public TextMessage convertToTextMessage(WebSocketMessages message) {
        try {
            return new TextMessage(objectMapper.writeValueAsString(message));
        } catch(JsonProcessingException e) {
            throw new MessageConversionException("Failed to convert WebSocketMessage to TextMessage");
        }
    }
}
