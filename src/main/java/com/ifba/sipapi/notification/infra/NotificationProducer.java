package com.ifba.sipapi.notification.infra;

import com.ifba.sipapi.notification.dto.UserNotificationDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class NotificationProducer {

    private final KafkaTemplate<String, UserNotificationDto> kafka;

    @Value("${spring.kafka.consumer.notification.topic}")
    private String topic;

    public NotificationProducer(KafkaTemplate<String, UserNotificationDto> kafka) {
        this.kafka = kafka;
    }

    public void send(UserNotificationDto event) {
        log.info("[start] NotificationProducer - send {}", event.email());
        kafka.send(topic, event);
        log.debug("[finish] NotificationProducer - send {}", event.email());
    }
}
