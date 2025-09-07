package com.ifba.sipapi.notification.application;

public record UserNotificationDto (String content, Boolean isAdmin, String email) {};
