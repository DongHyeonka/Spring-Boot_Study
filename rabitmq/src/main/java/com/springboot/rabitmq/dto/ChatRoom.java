package com.springboot.rabitmq.dto;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ChatRoom {
    private String id;          // UUID 등으로 생성
    private String roomName;
    private String createdBy;
    private Set<String> participants;

    public ChatRoom() {
        participants = new HashSet<>();
    }
}
