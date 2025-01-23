package com.springboot.querydsl.repository.product;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.springboot.querydsl.entity.Order.OrderStatus;
import com.springboot.querydsl.repository.product.query.ProductResponse;
import com.springboot.querydsl.repository.product.query.QProductResponse;

import lombok.RequiredArgsConstructor;

import static com.springboot.querydsl.entity.QProduct.product;
import static com.springboot.querydsl.entity.QOrderItem.orderItem;
import static com.springboot.querydsl.entity.QCategory.category;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements QuerydslProductRepository{
    private final JPAQueryFactory queryFactory;

    // 서브쿼리: 각 상품별 총 주문 수량을 계산
        // 1. JPAExpressions로 서브쿼리 시작
        // 2. orderItem.quantity.sum()으로 주문 수량 합계 계산
        // 3. orderItem.product.eq(product)로 현재 상품과 매칭되는 주문 아이템만 선택
        // 4. Expressions.asNumber()로 서브쿼리 결과를 NumberExpression<Integer>로 변환
    /**
     * 문제점
     * 1. 주문이 없는 상품의 경우 totalQuantity가 null이 되어 정렬 시 문제가 발생할 수 있음
     * 2. 성능상의 이슈 - 모든 상품에 대해 서브쿼리가 실행되므로 N+1 문제가 발생할 수 있음
     * 3. 메모리 사용량 증가 - 전체 상품 목록을 메모리에 로드
     * 4. 페이징 처리가 없어 대량의 데이터 조회 시 성능 저하
     * 5. 주문 취소된 주문도 포함되어 정확한 통계가 어려움
     * 
     * 개선사항
     * 1. coalesce를 사용하여 null 처리
     * 2. 서브쿼리 대신 join을 사용하여 N+1 문제 해결
     * 3. 페이징 처리 추가
     * 4. 주문 상태가 COMPLETED인 주문만 집계
     * 5. 성능 최적화를 위한 fetch join 사용
     */
    @Override
    public Page<ProductResponse> findMostOrderedProducts(Pageable pageable) {
        List<ProductResponse> content = queryFactory
                .select(new QProductResponse(
                    product.id,
                    product.name,
                    product.price,
                    product.category.name,
                    orderItem.quantity.sum().coalesce(0)
                ))
                .from(product)
                .leftJoin(product.category, category)
                .leftJoin(product.orderItems, orderItem)
                .where(orderItem.order.status.eq(OrderStatus.COMPLETED))
                .groupBy(
                    product.id,
                    product.name,
                    product.price,
                    product.category.name
                )
                .orderBy(orderItem.quantity.sum().coalesce(0).desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .where(orderItem.order.status.eq(OrderStatus.COMPLETED));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 문제점
     * 1. QProductResponse 생성자에 totalQuantity 파라미터가 누락되어 컴파일 에러 발생
     * 2. product.tags.contains()는 단일 태그만 검색 가능하나 List<String>을 전달하여 컴파일 에러 발생
     * 3. 태그 검색 시 대소문자 구분이 되어 검색 결과가 부정확할 수 있음
     * 4. 카테고리 정보를 가져오기 위한 join이 누락되어 N+1 문제 발생 가능
     * 5. 페이징 처리가 없어 대량의 데이터 조회 시 성능 저하 우려
     * 6. 태그가 없는 경우에 대한 예외 처리 누락
     */
    @Override
    public Page<ProductResponse> searchProductsByTags(List<String> tags, Pageable pageable) {

        List<String> lowercaseTags = tags.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toList());
        
        // 메인 쿼리
        List<ProductResponse> content = queryFactory
                .select(new QProductResponse(
                    product.id,
                    product.name,
                    product.price,
                    product.category.name,
                    Expressions.asNumber(0)  // totalQuantity 기본값 설정
                ))
                .from(product)
                .leftJoin(product.category, category)  // N+1 문제 방지를 위한 조인
                .where(
                    product.tags.any().lower().in(lowercaseTags)  // 대소문자 구분 없이 검색
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        
        // 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .where(
                    product.tags.any().lower().in(lowercaseTags)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
