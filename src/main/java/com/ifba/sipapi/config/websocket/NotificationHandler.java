package com.ifba.sipapi.config.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationHandler extends TextWebSocketHandler {

    private final WebSocketBroadcaster broadcaster;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("[start] NotificationHandler - afterConnectionEstablished");
        var channel = (String) session.getAttributes().getOrDefault("channel", "common");
        log.info("WS connected id={} channel={}", session.getId(), channel);
        broadcaster.register(session);
        log.debug("[finish] NotificationHandler - afterConnectionEstablished");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("[start] NotificationHandler - afterConnectionClosed");
        broadcaster.unregister(session);
        log.info("WS closed: session={}, status={}", session.getId(), status);
        log.debug("[finish] NotificationHandler - afterConnectionClosed");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("[start] NotificationHandler - handleTextMessage");
        log.info("📩 {}", message.getPayload());
        log.debug("[finish] NotificationHandler - handleTextMessage");
    }
}

