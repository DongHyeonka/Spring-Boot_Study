package com.springboot.rabitmq.service.domain;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ChatRoomService {
    private final Map<String, Set<String>> roomUsers = new ConcurrentHashMap<>();
    private final Map<String, UserStatus> userStatuses = new ConcurrentHashMap<>();
    
    @Data
    @AllArgsConstructor
    public class UserStatus {
        private String sessionId;
        private boolean online;
        private LocalDateTime lastSeen;
    }
    
    public void handleUserConnection(String username, String sessionId) {
        userStatuses.put(username, new UserStatus(sessionId, true, LocalDateTime.now()));
        log.info("[Chat] 사용자 접속: {}", username);
    }
    
    public void handleUserDisconnection(String username, String sessionId) {
        UserStatus status = userStatuses.get(username);
        if (status != null && status.getSessionId().equals(sessionId)) {
            status.setOnline(false);
            status.setLastSeen(LocalDateTime.now());
            log.info("[Chat] 사용자 오프라인: {}", username);
        }
    }
    
    public void addUserToRoom(String roomId, String username) {
        roomUsers.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(username);
        log.info("[Chat] 방 {} 에 사용자 {} 입장", roomId, username);
    }
    
    public void removeUserFromRoom(String roomId, String username) {
        Set<String> users = roomUsers.get(roomId);
        if (users != null) {
            users.remove(username);
            log.info("[Chat] 방 {} 에서 사용자 {} 퇴장", roomId, username);
            
            // 방이 비었으면 방 제거
            if (users.isEmpty()) {
                roomUsers.remove(roomId);
                log.info("[Chat] 빈 방 {} 제거", roomId);
            }
        }
    }
    
    public Set<String> getRoomUsers(String roomId) {
        return roomUsers.getOrDefault(roomId, Collections.emptySet());
    }
    
    public boolean isUserOnline(String username) {
        UserStatus status = userStatuses.get(username);
        return status != null && status.isOnline();
    }
}
