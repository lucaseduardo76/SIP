package com.ifba.sipapi.config.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

@Configuration
public class WebSocketMessageSecurityConfig {

    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager() {
        var messages = MessageMatcherDelegatingAuthorizationManager.builder();

        messages
                // allow connection management frames
                .simpTypeMatchers(SimpMessageType.CONNECT, SimpMessageType.HEARTBEAT,
                        SimpMessageType.UNSUBSCRIBE, SimpMessageType.DISCONNECT).permitAll()

                // SEND to @MessageMapping destinations (prefix /app)
                .simpTypeMatchers(SimpMessageType.MESSAGE).denyAll()

                // SUBSCRIBE to broker destinations (prefix /topic, /queue, /user)
                .simpSubscribeDestMatchers("/admin/**").hasRole("ADMIN")
                .simpSubscribeDestMatchers("/common/**").authenticated()

                .anyMessage().denyAll();

        return messages.build();
    }
}
