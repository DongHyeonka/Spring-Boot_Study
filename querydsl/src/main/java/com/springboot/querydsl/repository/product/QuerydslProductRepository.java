package com.springboot.querydsl.repository.product;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.springboot.querydsl.repository.product.query.ProductResponse;

public interface QuerydslProductRepository {
    Page<ProductResponse> findMostOrderedProducts(Pageable pageable);
    Page<ProductResponse> searchProductsByTags(List<String> tags, Pageable pageable);
}
