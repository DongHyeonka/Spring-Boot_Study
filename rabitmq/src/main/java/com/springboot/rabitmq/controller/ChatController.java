package com.springboot.rabitmq.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.rabitmq.dto.ChatMessage;
import com.springboot.rabitmq.publisher.MessagePublisher;
import com.springboot.rabitmq.service.application.ChatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {
    private final MessagePublisher messagePublisher;
    private final ChatService chatService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(ChatMessage chatMessage) {
        messagePublisher.sendPersistentMessage(chatMessage);
    }
}
