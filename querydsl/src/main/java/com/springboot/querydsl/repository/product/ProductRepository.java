package com.springboot.querydsl.repository.product;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.querydsl.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long>, QuerydslProductRepository {
    
}
