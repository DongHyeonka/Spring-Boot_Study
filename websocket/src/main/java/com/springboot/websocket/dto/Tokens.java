package com.springboot.websocket.dto;

import lombok.Getter;

@Getter
public class Tokens {
    private final AccessToken accessToken;
    private final RefreshToken refreshToken;

    public Tokens(AccessToken accessToken, RefreshToken refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
