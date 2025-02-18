package com.springboot.rabitmq.utils;

import java.io.IOException;
import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WebSocketSessionRegistry {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Instant> sessionLastActivity = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> userSessions = new ConcurrentHashMap<>();
    
    private static final Duration SESSION_TIMEOUT = Duration.ofMinutes(30);
    private static final int MAX_SESSIONS_PER_USER = 5;
    
    public void registerSession(String sessionId, WebSocketSession session) {
        String username = getUsername(session);
        if (countUserSessions(username) >= MAX_SESSIONS_PER_USER) {
            disconnectOldestSession(username);
        }
        sessions.put(sessionId, session);
        sessionLastActivity.put(sessionId, Instant.now());
        userSessions.computeIfAbsent(username, k -> 
            Collections.newSetFromMap(new ConcurrentHashMap<>()))
            .add(sessionId);
    }

    private int countUserSessions(String username) {
        Set<String> userSessionIds = userSessions.get(username);
        return userSessionIds != null ? userSessionIds.size() : 0;
    }
    
    private void disconnectOldestSession(String username) {
        Set<String> userSessionIds = userSessions.get(username);
        if (userSessionIds != null && !userSessionIds.isEmpty()) {
            String oldestSessionId = userSessionIds.stream()
                .min((s1, s2) -> sessionLastActivity.get(s1)
                    .compareTo(sessionLastActivity.get(s2)))
                .orElse(null);
                
            if (oldestSessionId != null) {
                closeSession(oldestSessionId);
                userSessionIds.remove(oldestSessionId);
            }
        }
    }
    
    public void removeSession(String sessionId) {
        WebSocketSession session = sessions.remove(sessionId);
        sessionLastActivity.remove(sessionId);

        if (session != null) {
            String username = getUsername(session);
            Set<String> userSessionIds = userSessions.get(username);
            if (userSessionIds != null) {
                userSessionIds.remove(sessionId);
                if (userSessionIds.isEmpty()) {
                    userSessions.remove(username);
                }
            }
        }
    }
    
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void cleanInactiveSessions() {
        Instant now = Instant.now();
        sessionLastActivity.forEach((sessionId, lastActivity) -> {
            if (now.isAfter(lastActivity.plus(SESSION_TIMEOUT))) {
                closeSession(sessionId);
            }
        });
    }
    
    public void closeSession(String sessionId) {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                session.close(CloseStatus.SESSION_NOT_RELIABLE);
            } catch (IOException e) {
                log.error("세션 종료 실패: {}", e.getMessage());
            }
        }
        removeSession(sessionId);
    }

    public WebSocketSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    private String getUsername(WebSocketSession session) {
        Principal principal = session.getPrincipal();
        if (principal != null) {
            return principal.getName(); // Spring Security Principal에서 username 가져오기
        }
        return null; 
    }
}