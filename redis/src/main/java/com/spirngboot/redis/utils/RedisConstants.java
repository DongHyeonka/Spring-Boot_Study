package com.spirngboot.redis.utils;

/**
 * Redis 상수 클래스
 * 
 * 이 클래스는 Redis 키 생성 및 관리에 필요한 상수를 정의합니다.
 */
public final class RedisConstants {
    // 상수 클래스는 인스턴스화 방지
    private RedisConstants() {
        throw new IllegalStateException("Utility class");
    }

    // 서비스 식별자
    public static final String SERVICE_ID = "auth";

    // 키 접두사
    public static final class Prefix {
        public static final String ACCESS_TOKEN = "access";
        public static final String REFRESH_TOKEN = "refresh";
        public static final String USER_SESSION = "session";
        public static final String BLACKLIST = "blacklist";
        public static final String ROLE = "role";
        public static final String TOKEN_EXPIRY = "token:expiry";
    }

    // TTL (초 단위)
    public static final class TTL {
        public static final long ACCESS_TOKEN = 3600;        // 1시간
        public static final long REFRESH_TOKEN = 1209600;    // 2주
        public static final long USER_SESSION = 3600;        // 1시간
        public static final long BLACKLIST = 3600;          // 1시간
    }

    // 구분자
    public static final String DELIMITER = ":";
}
