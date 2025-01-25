package com.springboot.websocket.exception;

public class MessageConversionException extends RuntimeException {
    public MessageConversionException(String message) {
        super(message);
    }
}
