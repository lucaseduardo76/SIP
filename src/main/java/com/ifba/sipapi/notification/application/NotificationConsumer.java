package com.ifba.sipapi.notification.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ifba.sipapi.config.websocket.WebSocketBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final WebSocketBroadcaster broadcaster;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${spring.kafka.consumer.notification.topic}",
            containerFactory = "${spring.kafka.consumer.notification.factory}"
    )
    public void onMessage(UserNotificationDto userNotificationDto) {
        try {
            String payload = objectMapper.writeValueAsString(userNotificationDto);

            if (userNotificationDto.isAdmin()) {
                broadcaster.broadcastToRoleInChannel("ROLE_ADMIN", "admin", payload);
            } else {
                broadcaster.broadcastToUser(userNotificationDto.email(), payload);
            }

        } catch (Exception ex) {
            System.err.println("[Kafka->WS] error: " + ex.getMessage());
        }
    }
}

