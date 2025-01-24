package com.springboot.querydsl.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;

@Repository
public abstract class QuerydslRepositorySupport {
    private final Class<?> domainClass;
    private EntityManager entityManager;
    private JPAQueryFactory queryFactory;
    private final PathBuilder<?> builder;

    public QuerydslRepositorySupport(Class<?> domainClass, JPAQueryFactory queryFactory, EntityManager entityManager) {
        Assert.notNull(domainClass, "Domain class must not be null!");
        this.domainClass = domainClass;
        this.queryFactory = queryFactory;
        this.entityManager = entityManager;
        this.builder = new PathBuilder<>(domainClass, domainClass.getSimpleName().toLowerCase());
    }

    @PostConstruct
    private void validate() {
        Assert.notNull(entityManager, "EntityManager must not be null!");
        Assert.notNull(queryFactory, "QueryFactory must not be null!");
    }

    // 동적 정렬 지원
    protected <T> JPAQuery<T> applySort(JPAQuery<T> query, Sort sort) {
        if (sort.isEmpty()) {
            return query;
        }

        for (Sort.Order order : sort) {
            ComparablePath<?> path = builder.getComparable(order.getProperty(), Comparable.class);
            query.orderBy(order.isAscending() ? path.asc() : path.desc());
        }
        return query;
    }

    // 동적 정렬 지원 (boolean 기반)
    protected <T> JPAQuery<T> applyBooleanSort(
        JPAQuery<T> query, 
        Map<String, Boolean> sortConditions
    ) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        
        for (Map.Entry<String, Boolean> condition : sortConditions.entrySet()) {
            ComparablePath<?> path = builder.getComparable(condition.getKey(), Comparable.class);
            orders.add(condition.getValue() ? path.desc() : path.asc());
        }
        
        return query.orderBy(orders.toArray(new OrderSpecifier[0]));
    }

    // 서브쿼리 지원
    protected <T> JPAQuery<T> selectSubQuery(Function<JPAQueryFactory, JPAQuery<T>> function) {
        return function.apply(queryFactory);
    }

    // 벌크 연산 지원
    protected long executeUpdate(Function<JPAQueryFactory, JPAUpdateClause> function) {
        long result = function.apply(queryFactory).execute();
        entityManager.flush();
        entityManager.clear();
        return result;
    }

    protected long executeDelete(Function<JPAQueryFactory, JPADeleteClause> function) {
        long result = function.apply(queryFactory).execute();
        entityManager.flush();
        entityManager.clear();
        return result;
    }

    // 페이징 최적화 지원
    protected <T> Page<T> applyPagination(
        Pageable pageable,
        Function<JPAQueryFactory, JPAQuery<T>> contentQuery,
        Function<JPAQueryFactory, JPAQuery<Long>> countQuery
    ) {
        JPAQuery<T> jpaContentQuery = contentQuery.apply(queryFactory);

        // 정렬 조건 적용
        if (pageable.getSort().isSorted()) {
            jpaContentQuery = applySort(jpaContentQuery, pageable.getSort());
        }

        List<T> content = jpaContentQuery
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> jpaCountQuery = countQuery.apply(queryFactory);

        return PageableExecutionUtils.getPage(content, pageable,
            () -> jpaCountQuery.fetchOne() != null ? jpaCountQuery.fetchOne() : 0L);
    }

    // 간단한 페이징 (카운트 쿼리 최적화 없음)
    protected <T> Page<T> applyPagination(
        Pageable pageable,
        Function<JPAQueryFactory, 
        JPAQuery<T>> contentQuery
    ) {
        final JPAQuery<T> jpaContentQuery = contentQuery.apply(queryFactory);

        if (pageable.getSort().isSorted()) {
            applySort(jpaContentQuery, pageable.getSort());
        }

        List<T> content = jpaContentQuery
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
            
        return PageableExecutionUtils.getPage(content, pageable, () -> 
            queryFactory
                .select(Expressions.constant(1L))
                .from((EntityPath<?>) jpaContentQuery.getMetadata().getJoins().get(0).getTarget())
                .fetchFirst() != null ? 1L : 0L);
    }

    // 캐시 지원
    protected <T> List<T> applyCache(JPAQuery<T> query, String cacheRegion) {
        query.setHint("org.hibernate.cacheable", true);
        query.setHint("org.hibernate.cacheRegion", cacheRegion);
        return query.fetch();
    }

    // 읽기 전용 트랜잭션 지원
    @Transactional(readOnly = true)
    protected <T> T readOnly(Supplier<T> supplier) {
        return supplier.get();
    }

    // 배치 처리 지원
    protected <T> void processByBatch(JPAQuery<T> query, int batchSize, Consumer<List<T>> consumer) {
        long offset = 0;
        List<T> batch;
        do {
            batch = query
                .offset(offset)
                .limit(batchSize)
                .fetch();
            consumer.accept(batch);
            offset += batchSize;
        } while (!batch.isEmpty() && batch.size() == batchSize);
    }

    protected JPAQuery<Long> fetchCount(Function<JPAQueryFactory, JPAQuery<Long>> countQuery) {
        return countQuery.apply(queryFactory);
    }

    protected JPAQueryFactory getQueryFactory() {
        return queryFactory;
    }
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    protected PathBuilder<?> getBuilder() {
        return builder;
    }

    protected Class<?> getDomainClass() {
        return domainClass;
    }

    protected <T> JPAQuery<T> select(Expression<T> expr) {
        return getQueryFactory().select(expr);
    }
    protected <T> JPAQuery<T> selectFrom(EntityPath<T> from) {
        return getQueryFactory().selectFrom(from);
    }
}
