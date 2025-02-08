package com.springboot.rabitmq.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Room {
    @Id
    private String id;             // 채팅 방 고유 번호 (UUID 등)

    private String roomName;       // 채팅 방 이름

    private String createdBy;      // 방 생성자

    // 채팅 방 참여자 목록
    @ElementCollection
    @CollectionTable(name = "room_participants")
    @Column(name = "participant")
    private Set<String> participants;

    protected Room() {
        participants = new HashSet<>();
    }
}