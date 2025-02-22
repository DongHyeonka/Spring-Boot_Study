package com.spirngboot.redis.utils;

import org.springframework.stereotype.Component;

/**
 * Redis 키 생성 유틸리티 클래스
 * 
 * 이 클래스는 Redis 키 생성 및 관리에 필요한 유틸리티 메서드를 제공합니다.
 */
@Component
public class RedisKeyGenerator {
    // Access Token 키 생성
    public String accessToken(String memberId) {
        return String.join(RedisConstants.DELIMITER, RedisConstants.SERVICE_ID, RedisConstants.Prefix.ACCESS_TOKEN, memberId);
    }

    // Refresh Token 키 생성
    public String refreshToken(String memberId) {
        return String.join(RedisConstants.DELIMITER, RedisConstants.SERVICE_ID, RedisConstants.Prefix.REFRESH_TOKEN, memberId);
    }

    // User Session 키 생성
    public String userSession(String memberId) {
        return String.join(RedisConstants.DELIMITER, RedisConstants.SERVICE_ID, RedisConstants.Prefix.USER_SESSION, memberId);
    }

    // Blacklist 키 생성
    public String blacklist(String token) {
        return String.join(RedisConstants.DELIMITER, RedisConstants.SERVICE_ID, RedisConstants.Prefix.BLACKLIST, token);
    }

    // Role 키 생성
    public String role(String roleType) {
        return String.join(RedisConstants.DELIMITER, RedisConstants.SERVICE_ID, RedisConstants.Prefix.ROLE, roleType);
    }

    // 키 패턴 생성 (검색용)
    public String pattern(String type) {
        return String.join(RedisConstants.DELIMITER, RedisConstants.SERVICE_ID, type, "*");
    }
}
