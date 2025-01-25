package com.springboot.websocket_stomp.provider;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class JwtTokenProvider implements AuthenticationProvider{

    @Value("${jwt.secret}")
    private String secretKey;

    public String createToken(String username, List<String> roles) {
        return null;
    }

    public Authentication getAuthentication(String token) {
        // UserDetails userDetails = new User(getUsername(token), "", getRoles(token));
        // return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        return null;
    }

    public String getUsername(String token) {
        return null;
    }

    public List<String> getRoles(String token) {
        return null;
    }

    public boolean validateToken(String token) {
        return false;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String token = (String) authentication.getPrincipal();
        if (validateToken(token)) {
            return getAuthentication(token);
        }
        throw new RuntimeException("Invalid JWT token");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
