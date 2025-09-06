package com.ifba.sipapi.config.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

// NotificationHandler.java
@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationHandler extends TextWebSocketHandler {

    private final WebSocketBroadcaster broadcaster;

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        broadcaster.unregister(session);
        log.info("❌ WS closed: session={}, status={}", session.getId(), status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Optional: echo or handle commands
        log.info("📩 {}", message.getPayload());
    }
}

