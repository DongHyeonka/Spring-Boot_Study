package com.spirngboot.redis.listener;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 키 만료 이벤트 리스너
 * 
 * 키 만료 이벤트 리스너는 Redis에서 키가 만료될 때 호출되는 이벤트 리스너입니다.
 * 이 리스너는 키 만료 이벤트를 수신하고, 해당 키의 타입에 따라 적절한 처리를 수행합니다.
 * 
 * 하지만 Redis의 키 만료 이벤트는 best-effort 방식 100프로 보장이 아님
 * 따라서 개선 방안이 필요함 -> 예를 들어 실패 시 재시도 매커니즘 추가, 주기적으로 만료 여부를 체크하는 배치 작업 또는 이벤트 기반 처리 외에도 폴링이나 TTL 확인 등
 * 2중으로 확인하는게 좋음.
 * TODO : 나중에 이 부분은 추후 개선하는 걸로 하고 일단 redis를 가지고 기본적인 인증된 사용자 관리하는 기능부터 작성하자...
 */
@Slf4j
@Component
public class CustomKeyExpirationEventListener extends KeyExpirationEventMessageListener {

    public CustomKeyExpirationEventListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("Key expired: {}", expiredKey);

        // 키 타입에 따른 처리
        if (expiredKey.contains("access")) {
            handleAccessTokenExpiration(expiredKey);
        } else if (expiredKey.contains("refresh")) {
            handleRefreshTokenExpiration(expiredKey);
        } else if (expiredKey.contains("session")) {
            handleSessionExpiration(expiredKey);
        }
    }

    private void handleAccessTokenExpiration(String key) {
        // Access Token 만료 처리 로직
        // 예시:
        // - 사용자 강제 로그아웃 처리
        // - 다른 서비스에 만료 알림
        // - 감사(audit) 로그 기록
        // - 통계 데이터 수집
        log.info("Access token expired: {}", key);
    }

    private void handleRefreshTokenExpiration(String key) {
        // Refresh Token 만료 처리 로직
        // 예시:
        // - 연관된 다른 서비스의 캐시 정리
        // - 다른 서비스에 만료 이벤트 전파
        log.info("Refresh token expired: {}", key);
    }

    private void handleSessionExpiration(String key) {
        // Session 만료 처리 로직
        // 예시:
        // - 연결된 웹소켓 세션 정리
        // - 사용자 상태 업데이트
        // - 리소스 정리
        log.info("Session expired: {}", key);
    }
}
