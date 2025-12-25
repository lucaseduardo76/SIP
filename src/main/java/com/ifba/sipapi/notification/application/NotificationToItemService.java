package com.ifba.sipapi.notification.application;

import com.ifba.sipapi.item.domain.item.Item;
import com.ifba.sipapi.item.domain.recoveryRequest.Recovery;
import com.ifba.sipapi.item.infra.item.ItemRepository;
import com.ifba.sipapi.notification.dto.ContentNotificationDto;
import com.ifba.sipapi.notification.dto.UserNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class NotificationToItemService {
    private final ItemRepository itemRepository;

    private final NotificationConsumer notificationConsumer;

    public void sendNotificationToRequester(Recovery recovery) {
        log.info("[start] NotificationToItemService - sendNotificationToRequester");
        Item item = itemRepository.findById(recovery.getItem().getId()).orElse(null);
        if(item == null) return;

        ContentNotificationDto contentToNotification = ContentNotificationDto.builder()
                .type(NotificationType.REQUEST_APPROVED)
                .itemId(item.getId())
                .itemName(recovery.getItem().getDescription())
                .claimScheduledTime(recovery.getRecoveryDateTime())
                .build();

        notificationConsumer.onMessage(new UserNotificationDto(contentToNotification, KindOfuser.COMMON, recovery.getUser().getEmail()));
        log.debug("[finish] NotificationToItemService - sendNotificationToRequester");
    }

    public void recoveryRejected(Recovery recovery, boolean isAcceptedToAnotherUser) {
        log.info("[start] NotificationToItemService - recoveryRejected");
        Item item = itemRepository.findById(recovery.getItem().getId()).orElse(null);
        if(item == null) return;

        ContentNotificationDto contentToNotification = ContentNotificationDto.builder()
                .type(NotificationType.REQUEST_REFUSED)
                .itemId(item.getId())
                .itemName(recovery.getItem().getDescription())
                .build();

        notificationConsumer.onMessage(new UserNotificationDto(contentToNotification, KindOfuser.COMMON, recovery.getUser().getEmail()));
        log.debug("[finish] NotificationToItemService - recoveryRejected");
    }

    public void requestCreated(final Item item) {
        log.info("[start] NotificationToItemService - requestCreated");
        ContentNotificationDto contentToNotification = ContentNotificationDto.builder()
                .type(NotificationType.NEW_REQUEST)
                .itemId(item.getId())
                .itemName(item.getDescription())
                .build();

        notificationConsumer.onMessage(new UserNotificationDto(contentToNotification, KindOfuser.ADMIN, null));
        log.debug("[finish] NotificationToItemService - requestCreated");
    }

    public void itemCreated(final Item item) {
        log.info("[start] NotificationToItemService - itemCreated");
        ContentNotificationDto contentToNotification = ContentNotificationDto.builder()
                .type(NotificationType.NEW_ITEM_CREATED)
                .itemId(item.getId())
                .itemName(item.getDescription())
                .build();

        notificationConsumer.onMessage(new UserNotificationDto(contentToNotification, KindOfuser.EVERYONE, null));
        log.debug("[finish] NotificationToItemService - itemCreated");
    }

    public void newItemOnCharity(Item item) {
        log.info("[start] NotificationToItemService - newItemOnCharity");
        ContentNotificationDto contentToNotification = ContentNotificationDto.builder()
                .type(NotificationType.NEW_ITEM_ON_CHARITY)
                .itemId(item.getId())
                .itemName(item.getDescription())
                .build();

        notificationConsumer.onMessage(new UserNotificationDto(contentToNotification, KindOfuser.EVERYONE, null));
        log.debug("[finish] NotificationToItemService - newItemOnCharity");
    }
}
