package com.springboot.websocket.dto;

import lombok.Getter;

@Getter
public class AccessToken {
    public enum TokenType {
        BEARER
    }

    private final String tokenValue;
    private final TokenType tokenType;

    public AccessToken(String tokenValue) {
        this.tokenValue = tokenValue;
        this.tokenType = TokenType.BEARER;
    }
}
