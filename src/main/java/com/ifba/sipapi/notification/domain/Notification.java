package com.ifba.sipapi.notification.domain;


import com.ifba.sipapi.Auditable;
import com.ifba.sipapi.item.domain.item.Item;
import com.ifba.sipapi.notification.application.KindOfuser;
import com.ifba.sipapi.notification.dto.ContentNotificationDto;
import com.ifba.sipapi.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@Table(name = "notification")
public class Notification extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    private LocalDateTime claimScheduledTime;
    private LocalDateTime readAt;
    private String claimer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusNotification status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner")
    private User owner;

    public Notification(NotificationType notificationType, LocalDateTime claimScheduledTime, Item item, User claimer, User owner) {
        this.type = notificationType;
        this.claimScheduledTime = claimScheduledTime;
        this.item = item;
        this.status = StatusNotification.PENDING;
        this.owner = owner;

        if (claimer != null) {
            this.claimer = claimer.getName();
        }

    }

    public void setAsRead(User user) {
        if(user.getId().equals(owner.getId())) {
            status = StatusNotification.READ;
            readAt = LocalDateTime.now();
        }
    }

    public ContentNotificationDto getNotificationContentDto(){
        ContentNotificationDto contentNotificationDto = new ContentNotificationDto(this);
        return  contentNotificationDto;
    }
}
