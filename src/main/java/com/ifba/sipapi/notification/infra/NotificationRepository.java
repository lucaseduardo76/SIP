package com.ifba.sipapi.notification.infra;

import com.ifba.sipapi.notification.domain.Notification;
import com.ifba.sipapi.notification.domain.StatusNotification;
import com.ifba.sipapi.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> getNotificationByOwner(User user);
    List<Notification> findAllByReadAtIsBeforeAndStatus(LocalDateTime now, StatusNotification statusNotification);
}
