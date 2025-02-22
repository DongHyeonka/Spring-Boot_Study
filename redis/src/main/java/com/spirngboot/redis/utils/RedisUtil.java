package com.spirngboot.redis.utils;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.spirngboot.redis.domain.RefreshToken;
import com.spirngboot.redis.dto.TokenBlacklist;
import com.spirngboot.redis.dto.TokenExpiry;

import lombok.RequiredArgsConstructor;
/**
 * Redis 유틸리티 클래스
 * 
 * 보통의 경우 redis는 jpa처럼 저장하는 방식, redisTemplate을 사용하는 방식으로 나뉜다.
 * 본인의 경우 redisTemplate을 사용하는 방식으로 구현하였다.
 * 
 * 참고로 redisTemplate을 사용하는 방식은 더 많은 기능을 사용할 수 있지만, 코드가 더 복잡해질 수 있다.
 * 누구든 redis를 사용할 때 꼭 필요한가를 먼저 검토해봐야 된다. 비용이 비싸기 때문에 꼭 필요하지 않다면 이런 비싼 비용을 지불하고 사용할 필요가 없다.
 * 본인은 이를 사용할지 안할지를 결정하기 위해 현재 프로젝트가 어떤식으로 구성이 되는지를 분석해보았다.
 * 먼저 프로젝트 구조상 msa 구조이며 인증 서버에서 인증 후 인증된 사용자를 관리하고 있는 구조였다. 검증은 각 msa 서버에서 진행되고 있었다.
 * 따라서 각 msa 서버에서 인증된 사용자가 리소스에 접근을 하려면 인증된 사용자를 관리하는 서버에 요청을 하고 응답을 받아야 한다.
 * 그럼 매번 인증서버를 통해서 인증된 사용자 인지 아닌지 확인을 해야 되는가? -> 이에 대한 답으로 redis를 사용하여 인증된 사용자를 관리하고 실질적인
 * 인증에 관련된 사용자 정보를 저장하는 역할을 인증서버에서 하고 각 msa에서는 redis에 저장된 인증된 사용자 정보를 조회하여 인증을 진행하는 방식으로 구현
 * 하는게 적합하다고 판단했다. 그럼 꼭 redis를 사용안해도 되는거 아닌가? postgresql DB에 저장하고 조회해도 되지 않나? 라고 생각이 들 수도 있다.
 * 인증된 사용자의 정보를 빈번하게 조회를 하는 msa 환경에서 redis를 사용하는 것이 적합하다고 판단했다. 연구에 따르면 인간은 100ms가 넘어가면 지연이
 * 발생한다고 인지한다고 한다.(여기서 말하는 연구는 실제로 어떤 연구인지는 모르겠으나 책에서 나왔음...) 
 * 따라서 데이터를 가져올 때 발생할 수 있는 지연은 0~1ms 이내여야한다. 그래서 redis를 사용하는게 적합하다고 판단했다.
 * 음 상황에 따라서는 postgresql DB에 저장하고 어플리케이션 레벨에서 캐싱 전략을 도입해도 될 수도 있겠다.
 * 
 * 여기서 주의할 점 redis는 싱글 스레드로 동작한다 이말이 참 어떻게 보면 동시성을 신경 안써도 되서 되게 좋다, 단일 코어에서도 높은 성능을 보여준다
 * 뭐 이렇게 생각할 거같은데 반대로 생각하면 싱글스레드로 동작하면서 하나의 커맨드가 10ms 걸리면 다른 사용자 요청은 대기를 해야된다 이 과정에서 예상치
 * 못한 오류가 발생할 수 있다는 점을 인지해야된다. -> 즉 구현을 할 때 지연시간이 최소가 되도록 구현을 하도록 신경을 써야된다.
 * 
 */
@Component
@RequiredArgsConstructor
public class RedisUtil {
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisKeyGenerator keyGenerator;

    public void setRefreshToken(String memberId, Authentication authentication) {
        String key = keyGenerator.refreshToken(memberId);
        redisTemplate.opsForValue().set(key, authentication, Duration.ofSeconds(RedisConstants.TTL.REFRESH_TOKEN));
    }

    public RefreshToken getRefreshToken(String memberId) {
        String key = keyGenerator.refreshToken(memberId);
        return (RefreshToken) redisTemplate.opsForValue().get(key);
    }

    public boolean deleteRefreshToken(String memberId) {
        String key = keyGenerator.refreshToken(memberId);
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    public boolean validateRefreshToken(String memberId, String refreshTokenValue) {
        RefreshToken storedRefreshToken = getRefreshToken(memberId);
        return storedRefreshToken != null && storedRefreshToken.getRefreshToken().equals(refreshTokenValue);
    }

    /**
     * 블랙리스트 토큰 설정
     * 이는 로그아웃과 관련된 설정임을 인지하자
     */
    public void setBlacklistToken(String accessToken, TokenBlacklist blacklist) {
        String key = keyGenerator.blacklist(accessToken);
        redisTemplate.opsForValue().set(key, blacklist, Duration.ofMillis(blacklist.getExpirationTime()));
    }

    public boolean isBlacklistToken(String accessToken) {
        String key = keyGenerator.blacklist(accessToken);
        return redisTemplate.hasKey(key);
    }

    // Sorted Set 토큰 만료 관리 용도임
    public void addTokenWithExpiry(TokenExpiry tokenExpiry) {
        redisTemplate.opsForZSet().add(RedisConstants.Prefix.TOKEN_EXPIRY, tokenExpiry.getToken(), tokenExpiry.getExpiryTime());
    }

    public Set<Object> getExpiredTokens(long currentTime) {
        return redisTemplate.opsForZSet().rangeByScore(RedisConstants.Prefix.TOKEN_EXPIRY,0, currentTime);
    }

    public void removeExpiredToken(TokenExpiry tokenExpiry) {
        redisTemplate.opsForZSet().remove(RedisConstants.Prefix.TOKEN_EXPIRY, tokenExpiry.getToken());
    }

    // ------------------------------------------------------------

    // Hash 작업 추가
    public void setHashField(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    public Object getHashField(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    public Map<Object, Object> getAllHashFields(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    // ------------------------------------------------------------

    public void addUserToRole(String role, String memberId) {
        String key = keyGenerator.role(role);
        redisTemplate.opsForSet().add(key, memberId);
    }

    public Set<Object> getUsersByRole(String role) {
        String key = keyGenerator.role(role);
        return redisTemplate.opsForSet().members(key);
    }

    public boolean isUserInRole(String role, String memberId) {
        String key = keyGenerator.role(role);
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, memberId));
    }

    public void removeUserFromRole(String role, String memberId) {
        String key = keyGenerator.role(role);
        redisTemplate.opsForSet().remove(key, memberId);
    }

    // ------------------------------------------------------------


    public void set(String key, Object value, long timeoutSeconds) {
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(timeoutSeconds));
    }
    
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }
    
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
