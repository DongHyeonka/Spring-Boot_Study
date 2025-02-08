package com.springboot.rabitmq.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.rabitmq.dto.ChatMessage;
import com.springboot.rabitmq.entity.Message;
import com.springboot.rabitmq.entity.Room;
import com.springboot.rabitmq.repository.ChatRepository;
import com.springboot.rabitmq.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public void saveMessage(ChatMessage chatMessage) {
        // chatRepository.save(message);
    }

    public List<Message> getMessages() {
        return chatRepository.findAll();
    }

    @Transactional
    public Room createRoom(String roomName, String creator) {
        Room room = Room.builder()
                .id(UUID.randomUUID().toString())
                .roomName(roomName)
                .createdBy(creator)
                .build();

        room.getParticipants().add(creator);

        // DB에 저장하여 생성된 채팅 방 반환
        return chatRoomRepository.save(room);
    }

    @Transactional
    public Room joinRoom(String roomId, String username) {
        Optional<Room> optionalRoom = chatRoomRepository.findById(roomId);
        if(optionalRoom.isPresent()){
            Room room = optionalRoom.get();
            room.getParticipants().add(username);
            return chatRoomRepository.save(room);
        }
        return null; // 채팅 방이 없으면 null 반환 또는 예외 처리 가능
    }

    @Transactional
    public Room leaveRoom(String roomId, String username) {
        Optional<Room> optionalRoom = chatRoomRepository.findById(roomId);
        if(optionalRoom.isPresent()){
            Room room = optionalRoom.get();
            room.getParticipants().remove(username);
            return chatRoomRepository.save(room);
        }
        return null;
    }
    
    public Room getRoom(String roomId) {
        return chatRoomRepository.findById(roomId).orElse(null);
    }
}
