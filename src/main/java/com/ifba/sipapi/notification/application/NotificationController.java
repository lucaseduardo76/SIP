package com.ifba.sipapi.notification.application;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationController {

    @MessageMapping("/notification.send")
    @SendTo("/topic/public")
    public UserNotificationDto send(UserNotificationDto dto) {
        return dto;
    }
}
