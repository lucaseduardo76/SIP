package com.ifba.sipapi.websocket;

import com.ifba.sipapi.config.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class StompJwtChannelInterceptor implements ChannelInterceptor {

    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String auth = accessor.getFirstNativeHeader("Authorization");
            if (auth == null) auth = accessor.getFirstNativeHeader("authorization");
            if (auth == null) throw new MessageDeliveryException("Missing Authorization header on CONNECT");

            String subject = tokenService.getSubject(auth);
            if (subject == null) throw new MessageDeliveryException("Invalid JWT");

            Collection authorities;
            try {
                UserDetails user = userDetailsService.loadUserByUsername(subject);
                authorities = user.getAuthorities();
            } catch (Exception ignore) {
                authorities = java.util.List.of();
            }

            Principal principal = new UsernamePasswordAuthenticationToken(subject, "N/A", authorities);
            accessor.setUser(principal);
        }
        return message;
    }
}
