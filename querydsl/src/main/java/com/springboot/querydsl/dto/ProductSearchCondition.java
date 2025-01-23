package com.springboot.querydsl.dto;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearchCondition {
    private String name;
    private Integer minPrice;
    private Integer maxPrice;
    private Long categoryId;
    private Set<String> tags;
}
