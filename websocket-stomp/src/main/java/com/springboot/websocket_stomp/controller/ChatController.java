package com.springboot.websocket_stomp.controller;

import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.websocket_stomp.dto.MessageDto;
import com.springboot.websocket_stomp.service.ChatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat.sendMessage")  // /app/chat.sendMessage로 들어오는 메시지 처리
    @SendTo("/topic/public")             // 처리 결과를 /topic/public으로 브로드캐스트 이 방법으로 사용해도 되고 messagingTemplate이걸로 전달해도 됨
    public MessageDto sendMessage(MessageDto messageDto) {
        return messageDto;  // 현재는 단순히 메시지 전달, 추가 로직 구현 가능
    }

    @MessageMapping("/private/{userId}")
    public void handlePrivateMessage(
        @DestinationVariable String userId,
        @Payload MessageDto message,
        Principal principal
    ) {
        message.setSender(principal.getName());
        message.setContent("Private message to " + userId);
        messagingTemplate.convertAndSendToUser(userId, "/queue/private", message);
    }

    @MessageMapping("/group/join/{roomId}")
    public void joinGroupChat(
        @DestinationVariable String roomId,
        Principal principal
    ) {
        // chatRoomService.joinRoom(roomId, principal.getName());
        notifyRoomMembers(roomId);
    }

    @MessageMapping("/group/leave/{roomId}")
    public void leaveGroupChat(
        @DestinationVariable String roomId,
        Principal principal
    ) {
        // chatRoomService.leaveRoom(roomId, principal.getName());
        notifyRoomMembers(roomId);
    }

    @MessageMapping("/group/{roomId}")
    @SendTo("/topic/group/{roomId}")
    public MessageDto handleGroupMessage(
        @DestinationVariable String roomId,
        @Payload MessageDto message,
        Principal principal
    ) {
        message.setSender(principal.getName());
        message.setContent("Group message to " + roomId);
        return message;
    }

    private void notifyRoomMembers(String roomId) {
        // 채팅방 멤버 목록 업데이트 알림
    }
}

