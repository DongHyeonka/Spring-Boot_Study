package com.springboot.websocket.service;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.UnaryOperator;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenProviderTemplate {
    private final SecretKey secretKey;
    private final Clock clock;
    private final long accessTokenExpirationMinutes;
    private final long refreshTokenExpirationMinutes;

    public TokenProviderTemplate(
        @Value("${secret.key}")String secretKey,
        @Value("${access.token.expiration.minutes}")long accessTokenExpirationMinutes,
        @Value("${refresh.token.expiration.minutes}")long refreshTokenExpirationMinutes,
        Clock clock
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.clock = clock;
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
        this.refreshTokenExpirationMinutes = refreshTokenExpirationMinutes;
    }
    
    public String generateToken(UnaryOperator<JwtBuilder> template, long expirationMinutes) {
        Instant now = clock.instant();
        Instant expiration = now.plus(expirationMinutes, ChronoUnit.MINUTES);
        JwtBuilder jwtBuilder = Jwts.builder()
            .claims()
                .expiration(Date.from(expiration))
                .issuedAt(Date.from(now))
                .and()
            .signWith(secretKey);
        JwtBuilder finalBuilder = template.apply(jwtBuilder);
        return finalBuilder.compact();
    }

    public String createAccessToken(UnaryOperator<JwtBuilder> template) {
        return generateToken(template, accessTokenExpirationMinutes);
    }

    public String createRefreshToken(UnaryOperator<JwtBuilder> template) {
        return generateToken(template, refreshTokenExpirationMinutes);
    }

    public Claims verifyAndGetClaims(String token) throws JwtException {
        return Jwts.parser()
            .verifyWith(secretKey)
            .clockSkewSeconds(60)
            .clock(() -> Date.from(clock.instant()))
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
