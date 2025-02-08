package com.springboot.rabitmq.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonSerialize
@JsonDeserialize
public class ChatMessage {
    public enum MessageType {
        UNKNOWN,
        JOIN,
        LEAVE,
        MESSAGE
    }

    private String content;
    private String sender;
    private MessageType type;
    private long timestamp;
    private String roomId;
    private String targetUserId;
}
