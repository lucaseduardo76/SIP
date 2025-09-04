package com.ifba.sipapi.config.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor;

@Configuration
class WebSocketAuthzInterceptorConfig {

    @Bean
    AuthorizationChannelInterceptor authorizationChannelInterceptor(
            AuthorizationManager<Message<?>> messageAuthorizationManager) {
        return new AuthorizationChannelInterceptor(messageAuthorizationManager);
    }
}
