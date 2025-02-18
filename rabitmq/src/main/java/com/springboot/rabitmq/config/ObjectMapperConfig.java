package com.springboot.rabitmq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class ObjectMapperConfig {
    // 이 Bean은 전역적으로 사용되는 ObjectMapper를 생성합니다.
    // JavaTimeModule을 등록하여 Java 8 날짜 및 시간 API (예: LocalDate, LocalDateTime 등)의 직렬화와 역직렬화를 지원합니다.
    // WRITE_DATES_AS_TIMESTAMPS 옵션을 비활성화하여 날짜 정보를 타임스탬프 대신 ISO-8601 형식으로 출력하도록 설정합니다.
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
