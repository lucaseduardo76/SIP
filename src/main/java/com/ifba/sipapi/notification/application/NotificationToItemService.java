package com.ifba.sipapi.notification.application;

import com.ifba.sipapi.item.domain.item.Item;
import com.ifba.sipapi.item.domain.recoveryRequest.Recovery;
import com.ifba.sipapi.item.infra.item.ItemRepository;
import com.ifba.sipapi.notification.domain.Notification;
import com.ifba.sipapi.notification.domain.NotificationType;
import com.ifba.sipapi.notification.dto.ContentNotificationDto;
import com.ifba.sipapi.notification.dto.UserNotificationDto;
import com.ifba.sipapi.notification.infra.NotificationRepository;
import com.ifba.sipapi.user.domain.Role;
import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.user.infra.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class NotificationToItemService {
    private final ItemRepository itemRepository;

    private final NotificationConsumer notificationConsumer;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public void sendNotificationToRequester(Recovery recovery) {
        log.info("[start] NotificationToItemService - sendNotificationToRequester");
        Item item = itemRepository.findById(recovery.getItem().getId()).orElse(null);
        if (item == null) return;

        Notification notification = new Notification(NotificationType.REQUEST_APPROVED,
                recovery.getRecoveryDateTime(),
                recovery.getItem(),
                recovery.getUser(),
                recovery.getUser());

        notification = notificationRepository.save(notification);
        notificationConsumer.onMessage(new UserNotificationDto(notification.getNotificationContentDto(), KindOfuser.COMMON, recovery.getUser().getEmail()));
        log.debug("[finish] NotificationToItemService - sendNotificationToRequester");
    }

    public void recoveryRejected(Recovery recovery, boolean isAcceptedToAnotherUser) {
        log.info("[start] NotificationToItemService - recoveryRejected");
        Item item = itemRepository.findById(recovery.getItem().getId()).orElse(null);
        if (item == null) return;

        Notification notification = new Notification(NotificationType.REQUEST_REFUSED,
                null,
                recovery.getItem(),
                recovery.getUser(),
                recovery.getUser());

        notification = notificationRepository.save(notification);
        notificationConsumer.onMessage(new UserNotificationDto(notification.getNotificationContentDto(), KindOfuser.COMMON, recovery.getUser().getEmail()));
        log.debug("[finish] NotificationToItemService - recoveryRejected");
    }

    public void requestCreated(final Item item, final User user) {
        log.info("[start] NotificationToItemService - requestCreated");

        userRepository.findByRole(Role.ADMIN).forEach(adminUser -> {
            Notification n = new Notification(NotificationType.NEW_REQUEST,
                    null,
                    item,
                    user,
                    adminUser);

            n = notificationRepository.save(n);
            notificationConsumer.onMessage(new UserNotificationDto(n.getNotificationContentDto(), KindOfuser.COMMON, adminUser.getEmail()));

        });



        log.debug("[finish] NotificationToItemService - requestCreated");
    }

    public void itemCreated(final Item item) {
        log.info("[start] NotificationToItemService - itemCreated");
        Notification notification = new Notification(NotificationType.NEW_ITEM_CREATED,
                null,
                item,
                null,
                null    );

        notificationConsumer.onMessage(new UserNotificationDto(notification.getNotificationContentDto(), KindOfuser.EVERYONE, null));
        log.debug("[finish] NotificationToItemService - itemCreated");
    }

    public void newItemOnCharity(Item item) {
        log.info("[start] NotificationToItemService - newItemOnCharity");
        Notification notification = new Notification(NotificationType.NEW_ITEM_ON_CHARITY,
                null,
                item,
                null,
                null);

        notificationConsumer.onMessage(new UserNotificationDto(notification.getNotificationContentDto(), KindOfuser.EVERYONE, null));
        log.debug("[finish] NotificationToItemService - newItemOnCharity");
    }
}
