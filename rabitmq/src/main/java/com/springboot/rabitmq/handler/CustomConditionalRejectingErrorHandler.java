package com.springboot.rabitmq.handler;

import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;

import com.springboot.rabitmq.handler.strategy.CustomFatalExceptionStrategy;

public class CustomConditionalRejectingErrorHandler extends ConditionalRejectingErrorHandler{
    public CustomConditionalRejectingErrorHandler() {
        super(new CustomFatalExceptionStrategy());
    }
}
