package com.springboot.websocket.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.springboot.websocket.dto.AccessToken;
import com.springboot.websocket.dto.RefreshToken;
import com.springboot.websocket.dto.Tokens;
import com.springboot.websocket.entity.Member;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtTokenService {
    private static final String ID = "id";
    private static final String AUTHORITIES = "authorities";
    private final TokenProviderTemplate tokenProviderTemplate;

    public Tokens createTokens(Member member) {
        String authorities = member.getAuthorityStrings().stream()
                .collect(Collectors.joining(","));

        String accessToken = tokenProviderTemplate.createAccessToken(
            jwtBuilder -> jwtBuilder
                .claims()
                .add(ID, member.getId())
                .add(AUTHORITIES, authorities)
                .and()
        );

        String refreshToken = tokenProviderTemplate.createRefreshToken(
            jwtBuilder -> jwtBuilder
                .claims()
                .add(ID, member.getId())
                .add(AUTHORITIES, authorities)
                .and()
        );
        
        Tokens tokens = new Tokens(
            new AccessToken(accessToken),
            new RefreshToken(refreshToken)
        );

        return tokens;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = tokenProviderTemplate.verifyAndGetClaims(token);

        String authoritiesString = claims.get(AUTHORITIES, String.class);

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(authoritiesString.split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

        User principal = new User(claims.get(ID).toString(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public Claims verifyAndGetClaims(String token) {
        return tokenProviderTemplate.verifyAndGetClaims(token);
    }
}
