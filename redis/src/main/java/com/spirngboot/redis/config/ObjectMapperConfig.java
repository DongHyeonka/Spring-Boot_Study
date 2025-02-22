package com.spirngboot.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class ObjectMapperConfig {

    @Bean
    public ObjectMapper objectMapper() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Object.class)
                .build();
        
        ObjectMapper objectMapper = new ObjectMapper(); // 시간 정보를 저장할 경우 기본적인 ObjectMapper를 사용하게 되면 오류가 발생할 수 있음
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // redis에 저장된 값을 다시 json 객체로 옮기는 과정에서 모르는 값이 있다? 바로 무효화 시킴 거의 필수적으로 작성된다고 생각하면 됨
        objectMapper.registerModule(new JavaTimeModule()); 
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL); // 객체를 저장할 때 필요한 타입을 같이 넘겨준다.
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 날짜를 timestamp가 아닌 ISO-8601 형식의 문자열로 직렬화하도록 설정 (예: "2023-10-27T10:00:00Z")

        return objectMapper;
    }
}
