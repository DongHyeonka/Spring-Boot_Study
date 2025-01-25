package com.springboot.websocket.dto;

import lombok.Getter;

@Getter
public class RefreshToken {
    private final String tokenValue;

    public RefreshToken(String tokenValue) {
        this.tokenValue = tokenValue;
    }
}
