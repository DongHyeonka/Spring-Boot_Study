package com.springboot.rabitmq.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnsCallback;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.springboot.rabitmq.callback.CustomConfirmCallback;
import com.springboot.rabitmq.callback.CustomReturnsCallback;
import com.springboot.rabitmq.settings.RabbitMQProperties;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RabbitMQProducerConfig {
    private final RabbitMQProperties properties;

    @Bean
    public RabbitTemplate rabbitTemplate(
        ConnectionFactory connectionFactory, 
        ConfirmCallback confirmCallback, 
        ReturnsCallback returnsCallback
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setMandatory(true);
        template.setReplyTimeout(properties.getReplyTimeout());
        template.setReceiveTimeout(properties.getReceiveTimeout());
        template.setConfirmCallback(confirmCallback);
        template.setReturnsCallback(returnsCallback);
        return template;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setCreateMessageIds(true);
        return converter;
    }

    @Bean
    public ConfirmCallback confirmCallback() {
        return new CustomConfirmCallback();
    }

    @Bean
    public ReturnsCallback returnsCallback() {
        return new CustomReturnsCallback();
    }
}
