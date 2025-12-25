package com.ifba.sipapi.notification.dto;

import com.ifba.sipapi.notification.application.KindOfuser;

public record UserNotificationDto (ContentNotificationDto content, KindOfuser kindOfuser, String email) {

};
