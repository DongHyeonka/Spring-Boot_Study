package com.springboot.rabitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.springboot.rabitmq.settings.RabbitMQProperties;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {
    // RabbitMQProperties를 통해 application.properties나 application.yml에 정의된 설정값을 주입받습니다.
    private final RabbitMQProperties rabbitMQProperties;

    // 채팅 메시지를 전송하기 위한 토픽 익스체인지 생성
    @Bean
    public TopicExchange chatExchange() {
        return ExchangeBuilder.topicExchange(rabbitMQProperties.getExchangeName())
            .durable(true) // 서버 재시작 후에도 익스체인지는 유지됩니다.
            .build();
    }

    // 일반(그룹) 채팅 메시지를 처리할 큐 생성
    @Bean
    public Queue generalQueue() {
        return QueueBuilder.durable(rabbitMQProperties.getGeneralQueueName())
            // 메시지 처리 실패 시 데드 레터 큐로 전달되도록 데드 레터 익스체인지 설정
            .withArgument("x-dead-letter-exchange", rabbitMQProperties.getDeadLetter().getExchange())
            // 데드 레터로 전달 시 사용할 라우팅 키 설정
            .withArgument("x-dead-letter-routing-key", rabbitMQProperties.getDeadLetter().getRoutingKey())
            // 메시지의 TTL(Time To Live) 설정 (밀리초 단위, 만료 후 데드 레터 큐로 이동)
            .withArgument("x-message-ttl", rabbitMQProperties.getMessageTtl())
            .build();
    }

    // 개인(1대1) 채팅 메시지를 처리할 큐 생성
    @Bean
    public Queue privateQueue() {
        return QueueBuilder.durable(rabbitMQProperties.getPrivateQueueName())
            // 메시지 처리 실패 시 데드 레터 큐로 전달되도록 데드 레터 익스체인지 설정
            .withArgument("x-dead-letter-exchange", rabbitMQProperties.getDeadLetter().getExchange())
            // 데드 레터로 전달 시 사용할 라우팅 키 설정
            .withArgument("x-dead-letter-routing-key", rabbitMQProperties.getDeadLetter().getRoutingKey())
            // 메시지의 TTL(Time To Live) 설정 (밀리초 단위, 만료 후 데드 레터 큐로 이동)
            .withArgument("x-message-ttl", rabbitMQProperties.getMessageTtl())
            .build();
    }

    // 데드 레터 큐 생성: 메시지 처리 실패나 TTL 만료 등의 경우 메시지를 보관
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(rabbitMQProperties.getDeadLetter().getQueue())
            .build();
    }

    // 데드 레터 익스체인지 생성: 데드 레터 큐로 메시지를 라우팅하기 위한 팬아웃 익스체인지 설정
    @Bean
    public FanoutExchange deadLetterExchange() {
        return ExchangeBuilder.fanoutExchange(rabbitMQProperties.getDeadLetter().getExchange())
            .durable(true) // 내구성 설정으로 서버 재시작 시에도 유지됩니다.
            .build();
    }

    // 일반 메시지 큐와 토픽 익스체인지를 연결하는 Binding 설정 (일반 채팅 메시지 라우팅)
    @Bean
    public Binding generalBinding() {
        return BindingBuilder.bind(generalQueue())
            .to(chatExchange())
            .with(rabbitMQProperties.getRoutingKey().getGeneralPattern());
    }

    // 개인 메시지 큐와 토픽 익스체인지를 연결하는 Binding 설정 (개인 채팅 메시지 라우팅)
    @Bean
    public Binding privateBinding() {
        return BindingBuilder.bind(privateQueue())
            .to(chatExchange())
            .with(rabbitMQProperties.getRoutingKey().getPrivatePattern());
    }

    // 데드 레터 큐와 데드 레터 익스체인지를 연결하는 Binding 설정
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
            .to(deadLetterExchange());
    }

    // 메시지 처리 실패 시, 실패한 메시지를 데드 레터 익스체인지로 재전송(republish)하기 위한 MessageRecoverer 설정
    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(
                rabbitTemplate,
                rabbitMQProperties.getDeadLetter().getExchange(),
                rabbitMQProperties.getDeadLetter().getRoutingKey()
            );
    }

    // 메시지의 직렬화 및 역직렬화를 JSON 형식으로 처리하기 위한 MessageConverter 설정 (Jackson2JsonMessageConverter 사용)
    @Bean
    public static MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setCreateMessageIds(true); // 메시지에 자동으로 고유 ID를 부여합니다.
        return converter;
    }
}
