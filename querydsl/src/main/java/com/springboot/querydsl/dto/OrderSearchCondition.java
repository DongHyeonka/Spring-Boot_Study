package com.springboot.querydsl.dto;

import java.time.LocalDateTime;

import com.springboot.querydsl.entity.Order.OrderStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderSearchCondition {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private OrderStatus status;
    private String city;
    private String street;
}
