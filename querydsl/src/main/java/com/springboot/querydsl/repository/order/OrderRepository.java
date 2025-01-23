package com.springboot.querydsl.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.querydsl.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long>, QuerydslOrderRepository {
    
}
