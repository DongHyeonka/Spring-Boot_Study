package com.springboot.rabitmq.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.rabitmq.entity.Message;

public interface ChatRepository extends JpaRepository<Message, Long> {
    
}
