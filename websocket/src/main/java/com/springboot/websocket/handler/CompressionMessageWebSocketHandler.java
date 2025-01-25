package com.springboot.websocket.handler;

import java.io.IOException;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

public class CompressionMessageWebSocketHandler extends WebSocketHandlerDecorator{

    // private final Inflater inflater = new Inflater();
    // private final byte[] buffer = new byte[1024];

    public CompressionMessageWebSocketHandler(WebSocketHandler delegate) {
        super(delegate);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            String payload = textMessage.getPayload();

            // 메시지 압축
            String compressedPayload = compress(payload);

            // 압축된 메시지로 새로운 TextMessage 생성
            TextMessage compressedMessage = new TextMessage(compressedPayload);

            // 메시지 압축 해제
            String decompressedPayload = decompress(compressedPayload);

            TextMessage decompressedMessage = new TextMessage(decompressedPayload);

            getDelegate().handleMessage(session, compressedMessage);
        } else {
            getDelegate().handleMessage(session, message);
        }
    }

    private String compress(String message) throws IOException {
        // Deflater를 사용한 압축 로직
        // ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // try (DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(outputStream)) {
        //     deflaterOutputStream.write(message.getBytes(StandardCharsets.UTF_8));
        // }
        // return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        return null;
    }

    private String decompress(String message) throws IOException {
        // Inflater를 사용한 압축 해제 로직
        // byte[] compressedBytes = Base64.getDecoder().decode(message);
        // ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedBytes);
        // ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // try (InflaterInputStream inflaterInputStream = new InflaterInputStream(inputStream, inflater)) {
        //     int length;
        //     while ((length = inflaterInputStream.read(buffer)) > 0) {
        //         outputStream.write(buffer, 0, length);
        //     }
        // } finally {
        //     inflater.reset(); // Inflater 재사용을 위해 리셋
        // }
        // return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        return null;
    }
}
