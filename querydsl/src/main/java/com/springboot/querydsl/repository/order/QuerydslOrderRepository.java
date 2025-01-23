package com.springboot.querydsl.repository.order;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.springboot.querydsl.dto.OrderSearchCondition;
import com.springboot.querydsl.entity.Order;
import com.springboot.querydsl.repository.order.query.OrderResponse;

public interface QuerydslOrderRepository {
    List<Order> search(OrderSearchCondition condition);
    Page<OrderResponse> searchPageSimple(OrderSearchCondition condition, Pageable pageable);
    Page<OrderResponse> searchPageComplex(OrderSearchCondition condition, Pageable pageable);
    List<OrderResponse> searchPageWithItems(OrderSearchCondition condition);
    Page<OrderResponse> searchPageWithItems(OrderSearchCondition condition, Pageable pageable);
}
