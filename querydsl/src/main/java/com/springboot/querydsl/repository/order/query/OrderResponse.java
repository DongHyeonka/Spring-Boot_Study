package com.springboot.querydsl.repository.order.query;

import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.core.annotations.QueryProjection;
import com.springboot.querydsl.entity.Address;
import com.springboot.querydsl.entity.Order.OrderStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderResponse {
    private Long id;
    private String userName;
    private OrderStatus status;
    private LocalDateTime orderDate;
    private Address deliveryAddress;
    private List<OrderItemResponse> orderItems;

    @QueryProjection
    public OrderResponse(
        Long id, 
        String userName, 
        OrderStatus status, 
        LocalDateTime orderDate, 
        Address deliveryAddress
    ) {
        this.id = id;
        this.userName = userName;
        this.status = status;
        this.orderDate = orderDate;
        this.deliveryAddress = deliveryAddress;
    }
}
