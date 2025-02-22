package com.spirngboot.redis.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenExpiry {
    private String token;
    private long expiryTime;
    private TokenType tokenType;

    @Builder
    public TokenExpiry(String token, long expiryTime, TokenType tokenType) {
        this.token = token;
        this.expiryTime = expiryTime;
        this.tokenType = tokenType;
    }
}
