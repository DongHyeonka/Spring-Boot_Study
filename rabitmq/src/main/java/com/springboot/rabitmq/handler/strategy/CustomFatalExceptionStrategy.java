package com.springboot.rabitmq.handler.strategy;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.MessageConversionException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomFatalExceptionStrategy extends ConditionalRejectingErrorHandler.DefaultExceptionStrategy {
    private static final String LOG_PREFIX = "[RabbitMQ]";

    @Override
    public boolean isFatal(Throwable t) {
        try {
            if (isCustomFatalException(t)) {
                logFatalError(t);
                log.info("{} 메시지가 DLQ로 전송됩니다.", LOG_PREFIX);
                return true;
            }
            return super.isFatal(t);
        } catch (Exception e) {
            log.error("치명적인 예외 처리 중 오류 발생: {}", LOG_PREFIX, e);
            return true;
        }
    }

    private boolean isCustomFatalException(Throwable t) {
        if (t instanceof MessageConversionException) {
            log.error("메시지 변환 실패: {}", t.getMessage());
            return true;
        }
        if (t instanceof IllegalArgumentException) {
            log.error("잘못된 인자 전달: {}", t.getMessage());
            return true;
        }
        if (t instanceof UnsupportedOperationException) {
            log.error("지원하지 않는 작업: {}", t.getMessage());
            return true;
        }
        return false;
    }

    private void logFatalError(Throwable t) {
        log.warn("{} 치명적인 예외 감지 - 타입: {}, 메시지: {}", 
            LOG_PREFIX,
            t.getClass().getSimpleName(),
            t.getMessage()
        );
    }

    @Override
    protected void logFatalException(ListenerExecutionFailedException ex, Throwable cause) {
        Message failedMessage = ex.getFailedMessage();
        MessageProperties props = failedMessage.getMessageProperties();
        
        log.error("{} 메시지 처리 실패 - DLQ로 전송 - ID: {}, Exchange: {}, RoutingKey: {}, 예외: {}", 
            LOG_PREFIX,
            props.getMessageId(),
            props.getReceivedExchange(),
            props.getReceivedRoutingKey(),
            cause.getMessage()
        );
    }
}
