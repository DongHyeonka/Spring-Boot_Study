package com.springboot.rabitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String CHAT_EXCHANGE = "chat.topic";
    public static final String GENERAL_QUEUE = "chat.general";
    public static final String PRIVATE_QUEUE = "chat.private";

    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(CHAT_EXCHANGE, true, false);
    }

    @Bean
    public Queue generalQueue() {
        return new Queue(GENERAL_QUEUE, true, false, false);
    }

    @Bean
    public Queue privateQueue() {
        return new Queue(PRIVATE_QUEUE, true, false, false);
    }

    // Binding 설정
    @Bean
    public Binding generalBinding() {
        return BindingBuilder.bind(generalQueue())
                .to(chatExchange())
                .with("chat.room.*");
    }

    @Bean
    public Binding privateBinding() {
        return BindingBuilder.bind(privateQueue())
                .to(chatExchange())
                .with("chat.user.#");
    }

    public static MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
