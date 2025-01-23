package com.springboot.querydsl.repository.product.query;

import java.util.Set;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private Integer price;
    private String categoryName;
    private Integer totalQuantity;
    private Set<String> tags;

    @QueryProjection
    public ProductResponse(Long id, String name, Integer price, String categoryName, Integer totalQuantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.categoryName = categoryName;
        this.totalQuantity = totalQuantity;
    }
}
