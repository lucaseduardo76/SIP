package com.ifba.sipapi.notification.api.service;

import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.config.security.TokenService;
import com.ifba.sipapi.notification.domain.Notification;
import com.ifba.sipapi.notification.domain.StatusNotification;
import com.ifba.sipapi.notification.dto.ContentNotificationDto;
import com.ifba.sipapi.notification.infra.NotificationRepository;
import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class NotificationApplicationService implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    @Override
    public List<ContentNotificationDto> getNotificationByuser(String token) {
        log.info("[start] NotificationApplicationService - getNotificationByuser");
        User user = userRepository.findByEmail(tokenService.getSubject(token)).orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        List<ContentNotificationDto> contentNotificationDto = notificationRepository.getNotificationByOwner(user).stream()
                        .map(ContentNotificationDto::new).toList();
        log.debug("[finish] NotificationApplicationService - getNotificationByuser");
        return contentNotificationDto;
    }

    @Override
    public void readNotification(String token, List<UUID> notificationId) {
        log.info("[start] NotificationApplicationService - readNotification");
        User user = userRepository.findByEmail(tokenService.getSubject(token)).orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        for(UUID nId : notificationId){
            Notification notification = notificationRepository.findById(nId).orElse(null);
            if(notification == null) continue;

            notification.setAsRead(user);
            notificationRepository.save(notification);
        }
        log.debug("[finish] NotificationApplicationService - readNotification");
    }

    @Override
    public void deleteReadNotifications() {
        log.info("[start] NotificationApplicationService - deleteReadNotifications");
        List<Notification> notificationList = notificationRepository.findAllByReadAtIsBeforeAndStatus(LocalDateTime.now(), StatusNotification.READ);


        notificationList.forEach(notification -> {
            if(notification.getReadAt().plusHours(8).isBefore(LocalDateTime.now()))
                notificationRepository.delete(notification);
        });

        log.debug("[finish] NotificationApplicationService - deleteReadNotifications");
    }
}
