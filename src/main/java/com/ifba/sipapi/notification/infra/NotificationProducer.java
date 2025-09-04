package com.ifba.sipapi.notification.infra;

import com.ifba.sipapi.notification.application.UserNotificationDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {

    private final KafkaTemplate<String, UserNotificationDto> kafka;

    @Value("${spring.kafka.consumer.notification.topic}")
    private String topic;

    public NotificationProducer(KafkaTemplate<String, UserNotificationDto> kafka) {
        this.kafka = kafka;
    }

    public void send(String content) {
        kafka.send(topic, new UserNotificationDto(content));
    }

    public void send(UserNotificationDto event) {
        kafka.send(topic, event);
    }
}
