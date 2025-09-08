package com.ifba.sipapi.notification.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

import java.security.Principal;

@Slf4j
@Controller
public class NotificationWsController {

    @EventListener
    public void onSocketConnected(final SessionConnectedEvent event) {
        Principal sessionUser = (Principal) event.getMessage().getHeaders().get("simpUser");

        if (sessionUser != null) {
            log.info("User connected: {}", sessionUser.getName());
        } else {
            log.warn("No user information found in the session.");
        }
    }
}
