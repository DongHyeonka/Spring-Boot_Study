package com.spirngboot.redis.dto;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenBlacklist {
    private String token;
    private Long expirationTime;
    private LocalDateTime blacklistedAt;

    @Builder
    public TokenBlacklist(String token, Long expirationTime, LocalDateTime blacklistedAt) {
        this.token = token;
        this.expirationTime = expirationTime;
        this.blacklistedAt = blacklistedAt;
    }
}
