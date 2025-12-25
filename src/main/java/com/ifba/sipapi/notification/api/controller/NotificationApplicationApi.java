package com.ifba.sipapi.notification.api.controller;

import com.ifba.sipapi.notification.api.service.NotificationService;
import com.ifba.sipapi.notification.dto.ContentNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Log4j2
public class NotificationApplicationApi implements NotificationApi {
    private final NotificationService notificationService;

    @Override
    public List<ContentNotificationDto> getNotificationByUser(String token) {
        log.info("[start] NotiticationApplictionApi - getNotificationByUser");
        List<ContentNotificationDto> notificationList = notificationService.getNotificationByuser(token);
        log.debug("[finish] NotiticationApplictionApi - getNotificationByUser");
        return notificationList;
    }

    @Override
    public void readNotification(String token, List<UUID> notificationId) {
        log.info("[start] NotiticationApplictionApi - readNotification");
        notificationService.readNotification(token, notificationId);
        log.debug("[finish] NotiticationApplictionApi - readNotification");
    }
}
