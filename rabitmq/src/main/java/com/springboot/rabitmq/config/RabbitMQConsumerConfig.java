package com.springboot.rabitmq.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;

import com.springboot.rabitmq.handler.CustomConditionalRejectingErrorHandler;
import com.springboot.rabitmq.interceptor.CustomMessageRecoverer;
import com.springboot.rabitmq.interceptor.MessageRetryOperationsInterceptor;
import com.springboot.rabitmq.settings.RabbitMQProperties;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConsumerConfig {
    // RabbitMQProperties: 어플리케이션 설정 파일에서 읽어들인 RabbitMQ 관련 설정 값들을 관리하는 빈
    private final RabbitMQProperties properties;

    // RabbitMQ 리스너 컨테이너 팩토리 빈 생성
    // 이 팩토리는 메시지를 소비하는 리스너의 실행 환경(연결, 메시지 변환, 동시성, 에러 처리, 재시도 정책 등)을 구성합니다.
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
        ConnectionFactory connectionFactory,   // RabbitMQ 서버와 연결하는 ConnectionFactory 빈
        MessageConverter messageConverter,       // 메시지 직렬화/역직렬화를 위한 MessageConverter 빈 (예: JSON 변환)
        MessageRetryOperationsInterceptor retryOperationsInterceptor,
        ErrorHandler errorHandler            // 에러 핸들러 빈
    ) {
        // 리스너 컨테이너 팩토리 객체 생성
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        
        // ── 기본 설정 ──
        // RabbitMQ 서버와의 연결 설정
        factory.setConnectionFactory(connectionFactory);
        // 메시지 변환기 설정: 메시지 수신 시 자동으로 형 변환(예: JSON을 Java 객체로 변환)
        factory.setMessageConverter(messageConverter);
        // prefetch count: 한 번에 소비자에게 전달할 메시지의 최대 개수를 설정 (예: 250개)
        factory.setPrefetchCount(properties.getPrefetchCount());
        // AcknowledgeMode.MANUAL: 수동 ACK 방식 사용으로, 메시지 처리가 완료되면 명시적으로 확인 응답을 보내야 함
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        // ── 동시성 설정 ──
        // 기본적으로 동시에 실행될 소비자 스레드 수 설정
        factory.setConcurrentConsumers(properties.getConcurrentConsumers());
        // 최대 동시에 실행될 소비자 스레드 수 설정
        factory.setMaxConcurrentConsumers(properties.getMaxConcurrentConsumers());

        // ConditionalRejectingErrorHandler: 메시지 처리 중 발생한 예외에 대해 조건에 따라 메시지 거부를 수행하여 DLQ로 보내도록 함
        factory.setErrorHandler(errorHandler);

        // ── 재시도 정책 설정 ──
        // RetryInterceptorBuilder를 사용하여 재시도 정책을 수립합니다.
        // 최대 3회까지 재시도하며, 지수형 백오프(ExponentialBackOffPolicy)를 적용하여 재시도 간격을 점진적으로 증가시킵니다.
        // 재시도 실패 시, recoverer에서 지정한대로 DLQ(Dead Letter Queue)로 메시지를 재전송합니다.
        factory.setAdviceChain(retryOperationsInterceptor.createRetryInterceptor());

        // 최종적으로 구성된 리스너 컨테이너 팩토리를 반환합니다.
        return factory;
    }

    @Bean
    public ErrorHandler errorHandler() {
        return new CustomConditionalRejectingErrorHandler();
    }

    @Bean
    public MessageRecoverer messageRecoverer(
        RabbitTemplate rabbitTemplate,
        RabbitMQProperties properties
    ) {
        return new CustomMessageRecoverer(rabbitTemplate, properties);
    }

    @Bean
    public MessageRetryOperationsInterceptor retryOperationsInterceptor(CustomMessageRecoverer messageRecoverer) {
        return new MessageRetryOperationsInterceptor(messageRecoverer);
    }
}
