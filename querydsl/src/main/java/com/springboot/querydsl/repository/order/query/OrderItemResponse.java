package com.springboot.querydsl.repository.order.query;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderItemResponse {
    private Long orderId;
    private String productName;
    private int quantity;
    private int price;

    @QueryProjection
    public OrderItemResponse(Long orderId, String productName, int quantity, int price) {
      this.orderId = orderId;
      this.productName = productName;
      this.quantity = quantity;
      this.price = price;
    }
}
