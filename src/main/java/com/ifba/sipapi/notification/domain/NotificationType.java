package com.ifba.sipapi.notification.domain;

public enum NotificationType {
    NEW_REQUEST("new_request"),
    REQUEST_APPROVED("request_approved"),
    REQUEST_REFUSED("request_refused"),
    REQUEST_REFUSED_ANOTHER_USER("request_refused_another_user"),
    NEW_ITEM_ON_CHARITY("new_item_on_charity"),
    NEW_ITEM_CREATED("new_item_created"),;

    private String description;

    NotificationType(String newRequest) {
        this.description = newRequest;
    }

    public String getDescription() {
        return description;
    }
}
