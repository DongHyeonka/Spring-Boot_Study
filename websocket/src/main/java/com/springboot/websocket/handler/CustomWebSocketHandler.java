package com.springboot.websocket.handler;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import com.springboot.websocket.dto.WebSocketMessages;
import com.springboot.websocket.utils.WebSocketMessageConverter;

import lombok.RequiredArgsConstructor;

/**
 * 추가적으로 고려할 사항
 * 1. 예외 처리: sendMessage 과정에서 예외가 발생할 수 있습니다. try-catch 블록을 사용하여 예외를 처리하고,
 *      필요하다면 로깅하거나, 에러 메시지를 클라이언트에게 전송하는 것을 고려해야 합니다.
 * 2. 세션 관리: 현재 코드는 각 세션을 개별적으로 처리합니다.
 *    만약 여러 세션을 관리하고 특정 세션에게 메시지를 보내거나, 특정 그룹의 세션에게 메시지를 브로드캐스팅하는 기능이 필요하다면,
 *    세션을 관리하는 별도의 자료구조 (예: List, Map)를 사용하여 세션을 추적하고 관리해야 합니다.
 * 3. 메시지 구조: 현재는 단순 텍스트 메시지만 처리하지만, 실제 애플리케이션에서는 JSON, XML 등 구조화된 메시지를 사용하는 경우가 많습니다.
 *    메시지 구조를 정의하고, 메시지를 파싱/직렬화하는 로직을 추가하여 효율적으로 데이터를 주고받을 수 있도록 개선할 수 있습니다.
 * 4. 보안: 웹소켓 연결도 보안이 중요합니다.
 *    필요에 따라 인증 (Authentication) 및 권한 부여 (Authorization) 메커니즘을 적용하여
 *    허가된 사용자만 웹소켓 서버에 접속하고 특정 기능을 사용할 수 있도록 제한해야 합니다.
 *    Spring Security와 같은 프레임워크를 사용하여 웹소켓 보안을 강화할 수 있습니다.
 * 5. 메시지 타입 분기 처리: 다양한 메시지 타입을 처리해야 할 경우, 메시지 payload를 파싱하여 특정 타입 필드를 확인하고,
 *    각 타입에 맞는 핸들러 로직을 호출하는 방식으로 개선할 수 있습니다.
 * 6. Ping/Pong 메시지 처리: 웹소켓 연결 유지를 위해 Ping/Pong 메시지를 주기적으로 주고받는 것을 고려할 수 있습니다.
 *    Spring WebSocket는 기본적으로 Ping/Pong을 처리하지만, 필요에 따라 커스터마이징할 수 있습니다.
 * 7. 에러 메시지 전송: 클라이언트 요청 처리 중 에러가 발생했을 때, 클라이언트에게 적절한 에러 메시지를 전송하여
 *    문제 상황을 알리고 디버깅을 돕는 것이 좋습니다.
 */

/**
 * AbstractWebSocketHandler는 WebSocketHandler 인터페이스를 구현하는 추상 클래스임
 * WebSocket 통신을 처리하는 핵심 로직을 직접 구현하는 데 사용
 * WebSocket 핸들러의 기본적인 틀을 제공하며, 개발자가 필요한 로직을 직접 작성
 * 유연성이 높지만, 모든 것을 직접 구현해야 하기 때문에 보일러플레이트 코드가 많아질 수 있음.
 * 
 * WebSocketHandlerDecorator는 WebSocketHandler 인터페이스를 구현하는 데코레이터 클래스임
 * 기존의 WebSocketHandler를 래핑하여 추가적인 기능을 제공하는 데 사용
 * 예를 들어, 보안 검증, 로깅, 에러 처리 등의 기능을 추가하여 코드의 재사용성을 높일 수 있음.
 * 즉 AbstractWebSocketHandler로 구현을 하되 추가적인 보안 검증, 로깅, 에러처리와 같은 로직은 WebSocketHandlerDecorator를 구현하는게 책임 분리에 있어서 좋을 듯
 */

@RequiredArgsConstructor
public class CustomWebSocketHandler extends AbstractWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(CustomWebSocketHandler.class);
    /**
     * WebSocketSession들을 저장하고 관리하기 위한 Set
     * ConcurrentHashMap을 이용하여 thread-safe하게 구현
     * 여러 WebSocket 연결을 동시에 처리할 수 있도록 함
     * ConcurrentHashMap으로 생성된 Map을 이용하여 Set을 생성
     * Set은 Map의 keySet() 뷰를 반환하며, Map의 키를 Set의 요소로 사용
     * ConcurrentHashMap을 사용했으므로, Set은 thread-safe 함
     */
    private final Set<WebSocketSession> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final WebSocketMessageConverter messageConverter;

    // 연결 관리 -------------------------처음 연결 시작 시 -------------------------------
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 연결 수립 시 로깅을 추가하여 연결 시작을 명확히 기록하는 것이 좋음.
        WebSocketMessages message = WebSocketMessages.builder()
            .type(WebSocketMessages.MessageType.ACK)
            .code("CONNECTION_ESTABLISHED")
            .message("WebSocket 연결이 수립되었습니다.")
            .sessionId(session.getId())
            .build();
        sessions.add(session);
        logger.info("WebSocket 연결이 수립되었습니다. Session ID: {}", session.getId());
        session.sendMessage(messageConverter.convertToTextMessage(message));
    }

    // 연결 관리 ------------------------- 연결 종료 시 -----------------------------------
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 연결 종료 시 추가적으로 session id를 로그에 포함하면 좋음.
        WebSocketMessages message = WebSocketMessages.builder()
            .type(WebSocketMessages.MessageType.ACK)
            .code("CONNECTION_CLOSED")
            .message("WebSocket 연결이 종료되었습니다.")
            .sessionId(session.getId())
            .build();
        sessions.remove(session);
        logger.info("WebSocket 연결이 종료되었습니다. Session ID: {}, 상태 코드: {}, 이유: {}", session.getId(), status.getCode(), status.getReason());
        session.sendMessage(messageConverter.convertToTextMessage(message));
    }

    /**
     * 텍스트 메시지 처리
     * TODO: 실제 애플리케이션에서는 수신한 텍스트 메시지를 기반으로 특정 비즈니스 로직을 수행해야 합니다.
     *      예를 들어, 채팅 메시지 처리, 게임 로직 수행, 데이터 업데이트 등 다양한 처리가 필요할 수 있습니다.
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            // 텍스트 메시지 수신 시 로그를 추가하여 어떤 메시지를 받았는지 기록하는 것이 좋음.
            logger.info("텍스트 메시지 수신: {}, Session ID: {}", payload, session.getId());
            if(payload.startsWith("TEXT|")) {
                handleTextPayload(session, payload.substring(5));
            } else {
                sendError(session, "UNSUPPORTED_FORMAT", "지원하지 않는 메시지 포맷입니다.");
            }
        } catch (Exception e) {
            logger.error("[Text Error] Session: {}", session.getId(), e);
            sendError(session, "PROCESSING_ERROR", "텍스트 메시지 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 바이너리 메시지 처리
     * TODO: 실제 애플리케이션에서는 바이너리 메시지를 처리해야 할 수 있습니다.
     *      예를 들어, 이미지, 파일 전송, 실시간 데이터 스트리밍 등 바이너리 데이터를 처리해야 할 수 있습니다.
     */
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        try {
            ByteBuffer buffer = message.getPayload();
            // 바이너리 메시지 수신 시 로그를 추가하여 어떤 메시지를 받았는지 기록하는 것이 좋음.
            logger.info("바이너리 메시지 수신, Session ID: {}", session.getId());
            session.sendMessage(new BinaryMessage(buffer));
        } catch (Exception e) {
            logger.error("[Binary Error] Session: {}", session.getId(), e);
            sendError(session, "BINARY_PROCESS_ERROR", "바이너리 메시지 처리 중 오류가 발생했습니다.");
        }
    }

    // 비즈니스 로직 ----------------------------------------------------------------
    private void handleTextPayload(WebSocketSession session, String payload) {
        WebSocketMessages ackMsg = WebSocketMessages.builder()
            .type(WebSocketMessages.MessageType.ACK)
            .code("JSON_RECEIVED")
            .message("JSON 파싱 완료")
            .data(payload)
            .sessionId(session.getId())
            .build();
        sendMessageWithHandling(session, ackMsg);
    }

    // 유틸리티 메서드 ---------------------------------------------------------------
    private void sendMessageWithHandling(WebSocketSession session, WebSocketMessages message) {
        try {
        if(!session.isOpen()) {
            logger.warn("메시지 전송 실패 - 세션이 닫힘: {}", session.getId());
            return;
        }

        TextMessage convertedMessage = messageConverter.convertToTextMessage(message);
        session.sendMessage(convertedMessage); // Send the message here


        } catch (Exception e) {
        logger.error("[sendMessageWithHandling Error] Session: {}, Message: {}", session.getId(), message, e);
        }
    }

    private void broadcastMessage(WebSocketMessages message) {
        sessions.forEach(session -> sendMessageWithHandling(session, message));
    }

    private void sendError(WebSocketSession session, String errorCode, String errorMessage) {
        WebSocketMessages errorMsg = WebSocketMessages.builder()
            .type(WebSocketMessages.MessageType.ERROR)
            .code(errorCode)
            .message(errorMessage)
            .sessionId(session.getId())
            .build();

        sendMessageWithHandling(session, errorMsg);
    }
}
