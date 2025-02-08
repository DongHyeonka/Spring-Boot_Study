package com.springboot.rabitmq.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.rabitmq.entity.Room;

public interface ChatRoomRepository extends JpaRepository<Room, String> {
    
}
