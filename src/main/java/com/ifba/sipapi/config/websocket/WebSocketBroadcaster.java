package com.ifba.sipapi.config.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketBroadcaster {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    void register(WebSocketSession s) { sessions.add(s); }
    void unregister(WebSocketSession s) { sessions.remove(s); }

    public void broadcast(String payload) {
        TextMessage msg = new TextMessage(payload);
        sessions.forEach(s -> {
            if (s.isOpen()) {
                try { s.sendMessage(msg); } catch (IOException ignored) {}
            }
        });
    }
}
