package com.ifba.sipapi.config.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ifba.sipapi.notification.dto.ContentNotificationDto;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class WebSocketBroadcaster {

    private final ObjectMapper objectMapper;

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

    public void broadcast(ContentNotificationDto payload) {
        log.info("[start] WebSocketBroadcaster - broadcast");
        sendWhere(payload, info -> true);
        log.debug("[finish] WebSocketBroadcaster - broadcast");
    }

    public void broadcastToChannel(String channel, ContentNotificationDto payload) {
        log.info("[start] WebSocketBroadcaster - broadcastToChannel");
        sendWhere(payload, info -> channel.equals(info.channel));
        log.debug("[finish] WebSocketBroadcaster - broadcastToChannel");
    }

    public void broadcastToRoleInChannel(String role, String channel, ContentNotificationDto payload) {
        log.info("[start] WebSocketBroadcaster - broadcastToRoleInChannel");
        sendWhere(payload, info -> channel.equals(info.channel) && info.roles.contains(role));
        log.debug("[finish] WebSocketBroadcaster - broadcastToRoleInChannel");
    }

    public void broadcastToUser(String username, ContentNotificationDto payload) {
        log.info("[start] WebSocketBroadcaster - broadcastToUser");
        sendWhere(payload, info -> Objects.equals(info.username, username));
        log.debug("[finish] WebSocketBroadcaster - broadcastToUser");
    }

    private void sendWhere(ContentNotificationDto payload, Predicate<SessionInfo> sessionInfoPredicate) {
        log.info("[start] WebSocketBroadcaster - sendWhere");
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            TextMessage msg = new TextMessage(jsonPayload);

            sessions.stream()
                    .filter(sessionInfoPredicate)
                    .forEach(info -> {
                        if (info.session.isOpen()) {
                            try {
                                info.session.sendMessage(msg); // envia a mensagem JSON
                            } catch (java.io.IOException ignored) {
                                log.info("[ignored] erro ao enviar mensagem JSON");
                            }
                        }
                    });

        } catch (Exception e) {
            log.error("Erro ao serializar ContentNotificationDto para JSON", e);
        }
        log.debug("[finish] WebSocketBroadcaster - sendWhere");
    }
}
