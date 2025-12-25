package com.ifba.sipapi.notification.api.controller;

import com.ifba.sipapi.docs.swagger.NotificationAPIDocs;
import com.ifba.sipapi.notification.dto.ContentNotificationDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/notification")
public interface NotificationApi {

    @NotificationAPIDocs.getNotificationByUser
    @GetMapping("/byUser")
    @ResponseStatus(HttpStatus.OK)
    List<ContentNotificationDto> getNotificationByUser(
            @RequestHeader(name = "Authorization", required = true) String token
    );

    //TODO CHANGE NAME
    @NotificationAPIDocs.getNotificationByUser
    @PatchMapping("/read")
    @ResponseStatus(HttpStatus.OK)
    void readNotification(
            @RequestHeader(name = "Authorization", required = true) String token,
            @RequestBody List<UUID> notificationId
    );


}
