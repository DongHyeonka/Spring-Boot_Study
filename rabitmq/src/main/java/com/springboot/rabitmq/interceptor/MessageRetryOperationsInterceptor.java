package com.springboot.rabitmq.interceptor;

import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageRetryOperationsInterceptor {
    private final CustomMessageRecoverer messageRecoverer;

    public RetryOperationsInterceptor createRetryInterceptor() {
        return RetryInterceptorBuilder.stateless()
            .maxAttempts(3)
            .backOffPolicy(createBackOffPolicy())
            .recoverer(messageRecoverer)
            .build();
    }

    private ExponentialBackOffPolicy createBackOffPolicy() {
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(10000);
        return backOffPolicy;
    }
}
