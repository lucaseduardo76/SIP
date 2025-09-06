package com.ifba.sipapi.notification.application;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

//    private final SimpMessagingTemplate simp;
//
//    public NotificationConsumer(SimpMessagingTemplate simp) { this.simp = simp; }
//
//    @KafkaListener(
//            topics = "${spring.kafka.consumer.notification.topic}",
//            containerFactory = "${spring.kafka.consumer.notification.factory}"
//    )
//    public void onMessage(UserNotificationDto e) {
//        simp.convertAndSend("/topic/public", new UserNotificationDto(e.content()));
//    }
}

