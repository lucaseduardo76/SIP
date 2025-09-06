package com.ifba.sipapi.notification.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

import java.security.Principal;

@Slf4j
@Controller
public class NotificationWsController {

    /* other parts of the code*/
    @EventListener
    public void onSocketConnected(final SessionConnectedEvent event) {
        // Retrieve the 'simpUser' attribute from the session headers
        Principal sessionUser = (Principal) event.getMessage().getHeaders().get("simpUser");

        if (sessionUser != null) {
            log.info("User connected: {}", sessionUser.getName());
            // Further processing if required
        } else {
            log.warn("No user information found in the session.");
        }
    }
}
