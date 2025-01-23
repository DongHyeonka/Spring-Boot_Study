package com.springboot.querydsl;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.querydsl.entity.Order.OrderStatus;
import com.springboot.querydsl.entity.Address;
import com.springboot.querydsl.entity.Category;
import com.springboot.querydsl.entity.Order;
import com.springboot.querydsl.entity.OrderItem;
import com.springboot.querydsl.entity.Product;
import com.springboot.querydsl.entity.Review;
import com.springboot.querydsl.entity.User;
import com.springboot.querydsl.entity.User.UserStatus;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InitData {
    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.dbInit1();
        initService.dbInit2();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {
        private final EntityManager em;

        public void dbInit1() {
        // 카테고리 생성
        Category electronics = createCategory("전자제품", null);
        Category computers = createCategory("컴퓨터", electronics);
        Category phones = createCategory("휴대폰", electronics);
        
        // 사용자 생성
        User user1 = createUser("user1@test.com", "사용자1", 30, UserStatus.ACTIVE, 
            new Address("서울", "강남대로", "12345"));
        
        // 상품 생성
        Product laptop = createProduct("노트북", 1200000, 100, computers, 
            Set.of("노트북", "전자제품", "컴퓨터"));
        Product phone = createProduct("스마트폰", 800000, 200, phones, 
            Set.of("휴대폰", "전자제품"));
        
        // 주문 생성
        Order order = createOrder(user1, OrderStatus.PAID, user1.getAddress());
        createOrderItem(order, laptop, 1, laptop.getPrice());
        createOrderItem(order, phone, 2, phone.getPrice());
        
        // 리뷰 생성
        createReview(user1, laptop, 5, "아주 좋은 노트북입니다.");
        createReview(user1, phone, 4, "만족스러운 휴대폰입니다.");

        em.persist(electronics);
        em.persist(computers);
        em.persist(phones);
        em.persist(user1);
        em.persist(laptop);
        em.persist(phone);
        em.persist(order);
        }

        public void dbInit2() {
        // 카테고리 생성
        Category fashion = createCategory("패션", null);
        Category clothing = createCategory("의류", fashion);
        Category accessories = createCategory("액세서리", fashion);
        
        // 사용자 생성
        User user2 = createUser("user2@test.com", "사용자2", 25, UserStatus.ACTIVE, 
            new Address("부산", "해운대로", "54321"));
        
        // 상품 생성
        Product shirt = createProduct("셔츠", 50000, 300, clothing, 
            Set.of("의류", "패션", "셔츠"));
        Product watch = createProduct("시계", 300000, 50, accessories, 
            Set.of("액세서리", "패션", "시계"));
        
        // 주문 생성
        Order order = createOrder(user2, OrderStatus.DELIVERED, user2.getAddress());
        createOrderItem(order, shirt, 3, shirt.getPrice());
        createOrderItem(order, watch, 1, watch.getPrice());
        
        // 리뷰 생성
        createReview(user2, shirt, 5, "품질이 좋은 셔츠입니다.");
        createReview(user2, watch, 4, "디자인이 예쁜 시계입니다.");

        em.persist(fashion);
        em.persist(clothing);
        em.persist(accessories);
        em.persist(user2);
        em.persist(shirt);
        em.persist(watch);
        em.persist(order);
        }

        private Category createCategory(String name, Category parent) {
            Category category = new Category(name, parent);
            return category;
        }

        private User createUser(String email, String name, int age, UserStatus status, Address address) {
            User user = new User(email, name, age, status, address, Set.of("ROLE_USER"));
            return user;
        }

        private Product createProduct(String name, int price, int stockQuantity, Category category, Set<String> tags) {
            Product product = new Product(name, price, stockQuantity, category, tags);
            return product;
        }

        private Order createOrder(User user, OrderStatus status, Address address) {
            Order order = new Order(user, status, LocalDateTime.now(), address);
            return order;
        }

        private OrderItem createOrderItem(Order order, Product product, int quantity, int price) {
            OrderItem orderItem = new OrderItem(order, product, quantity, price);
            order.getOrderItems().add(orderItem);
            return orderItem;
        }

        private Review createReview(User user, Product product, int rating, String content) {
            Review review = new Review(user, product, rating, content);
            em.persist(review);
            return review;
        }
    }
}
