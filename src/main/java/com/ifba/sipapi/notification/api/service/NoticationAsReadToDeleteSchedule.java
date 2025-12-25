package com.ifba.sipapi.notification.api.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@RequiredArgsConstructor
public class NoticationAsReadToDeleteSchedule {
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 1/12 * * *", zone = "America/Sao_Paulo")
    @Transactional
    public void deleteReadNotifications() {
        log.info("[start] NoticationAsReadToDeleteSchedule - deleteReadNotifications");
        notificationService.deleteReadNotifications();
        log.debug("[finish] NoticationAsReadToDeleteSchedule - deleteReadNotifications");
    }

}
