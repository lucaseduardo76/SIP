package com.ifba.sipapi.notification.api.service;

import com.ifba.sipapi.notification.dto.ContentNotificationDto;
import java.util.List;
import java.util.UUID;

public interface NotificationService {
    List<ContentNotificationDto> getNotificationByuser(String token);
    void readNotification(String token, List<UUID> notificationId);
    void deleteReadNotifications();
}
