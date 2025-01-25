package com.springboot.websocket.handler;

import java.io.IOException;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

public class CompressionLibraryWebSocketHandler extends WebSocketHandlerDecorator{

    public CompressionLibraryWebSocketHandler(WebSocketHandler delegate) {
        super(delegate);
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            String payload = textMessage.getPayload();

            // 메시지 압축
            String compressedPayload = compress(payload);

            TextMessage compressedMessage = new TextMessage(compressedPayload);

            // 압축 해제 시도
            String decompressedPayload = decompress(compressedPayload);

            TextMessage decompressedMessage = new TextMessage(decompressedPayload);

            // 메시지 처리를 위해 위임 핸들러에 전달
            getDelegate().handleMessage(session, decompressedMessage);
        } else {
            getDelegate().handleMessage(session, message);
        }
    }

    private String compress(String message) throws IOException {
        // Gzip 압축 로직
        // ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
        //     gzipOutputStream.write(message.getBytes("UTF-8"));
        // }

        // return outputStream.toString("ISO-8859-1"); // Gzip 압축 결과는 바이너리 데이터이므로 ISO-8859-1 인코딩 사용
        return null;
    }

    private String decompress(String message) throws IOException {
        // Gzip 압축 해제 로직
        // ByteArrayInputStream inputStream = new ByteArrayInputStream(message.getBytes("ISO-8859-1"));
        // ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // try (GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream)) {
        //     byte[] buffer = new byte[1024];
        //     int length;
        //     while ((length = gzipInputStream.read(buffer)) > 0) {
        //         outputStream.write(buffer, 0, length);
        //     }
        // } catch (ZipException e) {
        //     throw new IOException("Failed to decompress message: Invalid Gzip format", e);
        // }

        // return outputStream.toString("UTF-8");
        return null;
    }
}
