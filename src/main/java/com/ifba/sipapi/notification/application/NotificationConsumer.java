package com.ifba.sipapi.notification.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ifba.sipapi.config.websocket.WebSocketBroadcaster;
import com.ifba.sipapi.notification.dto.ContentNotificationDto;
import com.ifba.sipapi.notification.dto.UserNotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final WebSocketBroadcaster broadcaster;

    @KafkaListener(topics = "${spring.kafka.consumer.notification.topic}", containerFactory = "${spring.kafka.consumer.notification.factory}")
    public void onMessage(UserNotificationDto userNotificationDto) {
        try {
            final ContentNotificationDto payload = userNotificationDto.content();

            if (userNotificationDto.kindOfuser().equals(KindOfuser.ADMIN))
                broadcaster.broadcastToRoleInChannel("ROLE_ADMIN", "admin", payload);
             else if( userNotificationDto.kindOfuser().equals(KindOfuser.COMMON))
                broadcaster.broadcastToUser(userNotificationDto.email(), payload);
             else
                 broadcaster.broadcast(payload);

        } catch (Exception ex) {
            System.err.println("[Kafka->WS] error: " + ex.getMessage());
        }
    }
}

