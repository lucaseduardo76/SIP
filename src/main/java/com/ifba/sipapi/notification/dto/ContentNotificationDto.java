package com.ifba.sipapi.notification.dto;

import com.ifba.sipapi.notification.application.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ContentNotificationDto {

    private UUID itemId;
    private NotificationType type;
    private LocalDateTime claimScheduledTime;
    private String claimer;
    private String itemName;

}
