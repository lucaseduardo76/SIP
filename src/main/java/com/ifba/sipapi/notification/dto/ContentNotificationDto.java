package com.ifba.sipapi.notification.dto;

import com.ifba.sipapi.notification.domain.Notification;
import com.ifba.sipapi.notification.domain.NotificationType;
import com.ifba.sipapi.notification.domain.StatusNotification;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ContentNotificationDto {

    private UUID notificationId;
    private UUID itemId;
    private NotificationType type;
    private LocalDateTime claimScheduledTime;
    private String claimer;
    private String itemName;
    private StatusNotification status;
    private LocalDateTime createdAt;

    public ContentNotificationDto(Notification notification){
        this.notificationId = notification.getId();
        this.itemId = notification.getItem().getId();
        this.type = notification.getType();
        this.claimScheduledTime = notification.getClaimScheduledTime();
        this.itemName = notification.getItem().getDescription();
        this.claimer = notification.getClaimer();
        this.status = notification.getStatus();
        this.createdAt = notification.getCreatedAt();
    }
}
