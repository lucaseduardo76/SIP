package com.ifba.sipapi.config.websocket;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@Log4j2
@Component
public class WebSocketBroadcaster {

    private static final class SessionInfo {
        final WebSocketSession session;
        final String channel;
        final Set<String> roles;
        final String username;
        SessionInfo(WebSocketSession s, String ch, Set<String> rs, String u) {
            session = s; channel = ch; roles = rs; username = u;
        }
    }

    private final Set<SessionInfo> sessions = ConcurrentHashMap.newKeySet();

    public void register(WebSocketSession s) {
        log.info("[start] WebSocketBroadcaster - register");
        @SuppressWarnings("unchecked")
        var roles = (Set<String>) s.getAttributes().getOrDefault("roles", Set.of());
        var username = (String) s.getAttributes().get("username");
        var channel = (String) s.getAttributes().getOrDefault("channel", "common");
        sessions.add(new SessionInfo(s, channel, roles, username));
        log.info("[WS] registered channel={} total={}", channel, sessions.size());
        log.debug("[finish] WebSocketBroadcaster - register");
    }

    public void unregister(WebSocketSession s) {
        log.info("[start] WebSocketBroadcaster - unregister");
        sessions.removeIf(info -> info.session.getId().equals(s.getId()));
        log.debug("[finish] WebSocketBroadcaster - unregister");
    }

    public void broadcast(String payload) {
        log.info("[start] WebSocketBroadcaster - broadcast");
        sendWhere(payload, info -> true);
        log.debug("[finish] WebSocketBroadcaster - broadcast");
    }

    public void broadcastToChannel(String channel, String payload) {
        log.info("[start] WebSocketBroadcaster - broadcastToChannel");
        sendWhere(payload, info -> channel.equals(info.channel));
        log.debug("[finish] WebSocketBroadcaster - broadcastToChannel");
    }

    public void broadcastToRoleInChannel(String role, String channel, String payload) {
        log.info("[start] WebSocketBroadcaster - broadcastToRoleInChannel");
        sendWhere(payload, info -> channel.equals(info.channel) && info.roles.contains(role));
        log.debug("[finish] WebSocketBroadcaster - broadcastToRoleInChannel");
    }

    public void broadcastToUser(String username, String payload) {
        log.info("[start] WebSocketBroadcaster - broadcastToUser");
        sendWhere(payload, info -> Objects.equals(info.username, username));
        log.debug("[finish] WebSocketBroadcaster - broadcastToUser");
    }

    private void sendWhere(String payload, Predicate<SessionInfo> test) {
        log.info("[start] WebSocketBroadcaster - sendWhere");
        var msg = new TextMessage(payload);
        sessions.stream().filter(test).forEach(info -> {
            if (info.session.isOpen()) {
                try { info.session.sendMessage(msg); } catch (java.io.IOException ignored) {}
            }
        });
        log.debug("[finish] WebSocketBroadcaster - sendWhere");
    }
}
