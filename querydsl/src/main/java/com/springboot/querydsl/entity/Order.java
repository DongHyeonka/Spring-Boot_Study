package com.springboot.querydsl.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime orderDate;

    @Embedded
    private Address deliveryAddress;

    public Order(User user, OrderStatus status, LocalDateTime orderDate, Address deliveryAddress) {
        this.user = user;
        this.status = status;
        this.orderDate = orderDate;
        this.deliveryAddress = deliveryAddress;
    }

    public enum OrderStatus {
      PENDING, PAID, SHIPPED, DELIVERED, CANCELLED, COMPLETED
    }
}
