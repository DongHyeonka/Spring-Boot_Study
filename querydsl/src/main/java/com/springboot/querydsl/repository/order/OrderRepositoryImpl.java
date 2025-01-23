package com.springboot.querydsl.repository.order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.springboot.querydsl.dto.OrderSearchCondition;
import com.springboot.querydsl.entity.Order.OrderStatus;
import com.springboot.querydsl.entity.Order;
import com.springboot.querydsl.repository.order.query.OrderItemResponse;
import com.springboot.querydsl.repository.order.query.OrderResponse;
import com.springboot.querydsl.repository.order.query.QOrderItemResponse;
import com.springboot.querydsl.repository.order.query.QOrderResponse;

import lombok.RequiredArgsConstructor;

import static com.springboot.querydsl.entity.QOrder.order;
import static com.springboot.querydsl.entity.QUser.user;
import static com.springboot.querydsl.entity.QOrderItem.orderItem;

@RequiredArgsConstructor
public class OrderRepositoryImpl implements QuerydslOrderRepository{
    private final JPAQueryFactory queryFactory;

    
    /*
    *  주문 검색 조건에 맞는 주문 목록을 조회하는 메서드입니다.
    *  현재 코드는 Order 엔티티를 직접 반환하고 있습니다.
    *  이는 다음과 같은 문제점을 야기할 수 있습니다.
    *
    *  1.  엔티티 직접 노출:
    *      - 엔티티는 데이터베이스 테이블과 직접 매핑되는 객체이므로,
    *        외부에 직접 노출될 경우 데이터 구조가 변경될 때 API 변경이 불가피합니다.
    *      - 또한, 엔티티에는 민감한 정보가 포함될 수 있으므로 보안상 위험할 수 있습니다.
    *      - 불필요한 정보까지 클라이언트에 전달될 수 있습니다.
    *
    *  2.  지연 로딩 문제:
    *      - Order 엔티티와 연관된 User 엔티티를 지연 로딩으로 설정했을 경우,
    *        반환된 Order 엔티티를 사용하는 과정에서 추가적인 데이터베이스 쿼리가 발생할 수 있습니다.
    *        이는 N+1 문제로 이어져 성능 저하를 유발할 수 있습니다.
    *
    *  3.  DTO 사용의 필요성:
    *      - API 응답에는 필요한 데이터만 포함하는 것이 좋습니다.
    *      - 이를 위해 DTO(Data Transfer Object)를 사용하여 필요한 데이터만 선택적으로 반환해야 합니다.
    *      - DTO를 사용하면 클라이언트에게 필요한 데이터만 전달하고, 엔티티의 변경으로부터 API를 보호할 수 있습니다.
    *
    *  따라서, 이 메서드는 Order 엔티티를 직접 반환하는 대신,
    *  OrderResponse DTO를 반환하도록 변경해야 합니다.
    *  또한, 필요한 경우 fetch join을 사용하여 N+1 문제를 해결해야 합니다.
    */
    public List<Order> search(OrderSearchCondition condition) {
        return queryFactory
            .selectFrom(order)
            .leftJoin(order.user, user)
            .where(
                dateGoe(condition.getStartDate()),
                dateLoe(condition.getEndDate()),
                statusEq(condition.getStatus()),
                cityEq(condition.getCity()),
                streetContains(condition.getStreet())
            )
            .fetch();
    }

    /**
     * 개선된 searchPageSimple 메서드입니다.
     * 
     * 1. DTO 변환 최적화:
     *    - QOrderResponse를 사용하여 엔티티를 DTO로 직접 변환
     *    - 필요한 데이터만 선택적으로 조회하여 성능 향상
     * 
     * 2. N+1 문제 해결:
     *    - order.user에 대해 fetchJoin을 적용하여 연관된 User 엔티티를 한 번에 조회
     *    - 추가적인 쿼리 발생 방지
     * 
     * 3. 페이징 처리:
     *    - offset과 limit을 사용하여 효율적인 페이징 구현
     *    - 전체 카운트를 위한 별도의 최적화된 카운트 쿼리 사용
     * 
     * 4. 동적 쿼리 적용:
     *    - BooleanExpression을 사용한 where 절로 동적 쿼리 구현
     *    - 검색 조건에 따라 유연하게 쿼리 생성
     */
    @Override
    public Page<OrderResponse> searchPageSimple(OrderSearchCondition condition, Pageable pageable) {
        List<OrderResponse> content = queryFactory
                .select(new QOrderResponse(
                    order.id,
                    order.user.name,
                    order.status,
                    order.orderDate,
                    order.deliveryAddress
                ))
                .from(order)
                .leftJoin(order.user, user)
                .where(
                    dateGoe(condition.getStartDate()),
                    dateLoe(condition.getEndDate()),
                    statusEq(condition.getStatus()),
                    cityEq(condition.getCity()),
                    streetContains(condition.getStreet())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
            .select(order.count())
            .from(order)
            .where(
                dateGoe(condition.getStartDate()),
                dateLoe(condition.getEndDate()),
                statusEq(condition.getStatus()),
                cityEq(condition.getCity()),
                streetContains(condition.getStreet())
            )
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     *  개선된 `searchPageComplex` 메서드입니다.
     *  - 복잡한 검색 조건과 페이징 처리, 카운트 쿼리 최적화를 적용
     *  - `fetchJoin`을 사용하여 N+1 문제를 해결하고, 성능을 최적화
     *  - 카운트 쿼리에 `JPAQuery<Long>`을 사용하여 타입 안정성을 확보
     */
    @Override
    public Page<OrderResponse> searchPageComplex(OrderSearchCondition condition, Pageable pageable) {
        List<OrderResponse> content = queryFactory
                .select(new QOrderResponse(
                        order.id,
                        order.user.name,
                        order.status,
                        order.orderDate,
                        order.deliveryAddress
                ))
                .from(order)
                .leftJoin(order.user, user)
                .where(
                    dateGoe(condition.getStartDate()),
                    dateLoe(condition.getEndDate()),
                    statusEq(condition.getStatus()),
                    cityEq(condition.getCity()),
                    streetContains(condition.getStreet())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        
        /**
         *  카운트 쿼리 최적화:
         *  - `select(order.count())`를 사용하여 `JPAQuery<Long>` 타입으로 카운트 쿼리 생성
         *  - 불필요한 데이터 로딩 없이, count(*) 쿼리만 실행하여 성능 향상
         */
        JPAQuery<Long> countQuery = queryFactory // 여기서 조립만 하고 실행 x
                .select(order.count())
                .from(order)
                .where(
                    dateGoe(condition.getStartDate()),
                    dateLoe(condition.getEndDate()),
                    statusEq(condition.getStatus()),
                    cityEq(condition.getCity()),
                    streetContains(condition.getStreet())
                );
        
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne); // 이 시점에서 쿼리가 필요하면 알리고 아니면 안날리면 됌
    }

    /**
     * 주문과 주문 상품을 함께 조회하는 메서드입니다.
     * 
     * 1. 성능 최적화:
     *    - Order와 OrderItem을 별도의 쿼리로 조회하여 N+1 문제 해결
     *    - in절을 사용한 배치 조회로 쿼리 수 최소화
     *    
     * 2. 메모리 최적화:
     *    - OrderItem 정보를 Map으로 그룹화하여 효율적인 매핑 구현
     *    - Stream API를 활용한 데이터 처리
     *    
     * 3. 제한사항:
     *    - 페이징 처리가 되어있지 않아 대용량 데이터 조회시 성능 이슈 발생 가능
     *    - 필요한 경우 searchPageWithItems(condition, pageable) 메서드 사용 권장
     */
    @Override
    public List<OrderResponse> searchPageWithItems(OrderSearchCondition condition) {
        // 1. Order 정보 조회
        List<OrderResponse> orders = queryFactory
            .select(new QOrderResponse(
                    order.id,
                    order.user.name,
                    order.status,
                    order.orderDate,
                    order.deliveryAddress
            ))
            .from(order)
            .leftJoin(order.user, user)
            .where(
                dateGoe(condition.getStartDate()),
                dateLoe(condition.getEndDate()),
                statusEq(condition.getStatus()),
                cityEq(condition.getCity()),
                streetContains(condition.getStreet())
            )
            .fetch();
        
        // 2. OrderItem 정보 한 번에 조회
        Map<Long, List<OrderItemResponse>> orderItemMap = queryFactory
            .select(new QOrderItemResponse(
                orderItem.order.id,
                orderItem.product.name,
                orderItem.quantity,
                orderItem.price
            ))
            .from(orderItem)
            .where(orderItem.order.id.in(
                orders.stream()
                    .map(OrderResponse::getId)
                    .collect(Collectors.toList())
            ))
            .fetch()
            .stream()
            .collect(Collectors.groupingBy(OrderItemResponse::getOrderId));
        
        // 3. Order에 OrderItem 설정
        orders.forEach(order -> 
            order.getOrderItems().addAll(
                orderItemMap.getOrDefault(order.getId(), new ArrayList<>())
            ));

        return orders;
    }

    @Override
    public Page<OrderResponse> searchPageWithItems(OrderSearchCondition condition, Pageable pageable) {
        // 1. 페이징된 Order 정보 조회
        List<OrderResponse> orders = queryFactory
                .select(new QOrderResponse(
                        order.id,
                        order.user.name,
                        order.status,
                        order.orderDate,
                        order.deliveryAddress
                ))
                .from(order)
                .leftJoin(order.user, user)
                .where(
                    dateGoe(condition.getStartDate()),
                    dateLoe(condition.getEndDate()),
                    statusEq(condition.getStatus()),
                    cityEq(condition.getCity()),
                    streetContains(condition.getStreet())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        
        // 2. OrderItem 정보 한 번에 조회
        if (!orders.isEmpty()) {
            Map<Long, List<OrderItemResponse>> orderItemMap = queryFactory
                    .select(new QOrderItemResponse(
                            orderItem.order.id,
                            orderItem.product.name,
                            orderItem.quantity,
                            orderItem.price
                    ))
                    .from(orderItem)
                    .leftJoin(orderItem.order, order)
                    .where(orderItem.order.id.in(
                        orders.stream()
                            .map(OrderResponse::getId)
                            .collect(Collectors.toList())
                    ))
                    .fetch()
                    .stream()
                    .collect(Collectors.groupingBy(OrderItemResponse::getOrderId));
            
            // 3. Order에 OrderItem 설정
            orders.forEach(order -> 
                order.getOrderItems().addAll(
                    orderItemMap.getOrDefault(order.getId(), new ArrayList<>())
                ));
        }

        // 4. 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(order.count())
                .from(order);
        
        return PageableExecutionUtils.getPage(orders, pageable, countQuery::fetchOne);
    }

    private BooleanExpression dateGoe(LocalDateTime startDate) {
        return startDate != null ? order.orderDate.goe(startDate) : null;
    }
    
    private BooleanExpression dateLoe(LocalDateTime endDate) {
        return endDate != null ? order.orderDate.loe(endDate) : null;
    }
    
    private BooleanExpression statusEq(OrderStatus status) {
        return status != null ? order.status.eq(status) : null;
    }
    
    private BooleanExpression cityEq(String city) {
        return StringUtils.hasText(city) ? 
        order.deliveryAddress.city.eq(city) : null;
    }
    
    private BooleanExpression streetContains(String street) {
        return StringUtils.hasText(street) ? 
        order.deliveryAddress.street.contains(street) : null;
    }
}
