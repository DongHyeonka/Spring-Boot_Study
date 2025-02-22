package com.spirngboot.redis.domain;

import java.io.Serializable;

import com.spirngboot.redis.common.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseTimeEntity implements Serializable {
    private String memberId;
    private String refreshToken;
    private Long expiration;

    @Builder
    public RefreshToken(String memberId, String refreshToken, Long expiration) {
        this.memberId = memberId;
        this.refreshToken = refreshToken;
        this.expiration = expiration;
    }
}
