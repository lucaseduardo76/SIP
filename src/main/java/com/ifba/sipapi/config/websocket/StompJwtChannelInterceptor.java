package com.ifba.sipapi.websocket;

import com.ifba.sipapi.config.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StompJwtChannelInterceptor implements ChannelInterceptor {

    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;
    private final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var accessor = StompHeaderAccessor.wrap(message);
        var cmd = accessor.getCommand();

        if (StompCommand.CONNECT.equals(cmd)) {
            String auth = accessor.getFirstNativeHeader("Authorization");
            if (auth == null) auth = accessor.getFirstNativeHeader("authorization");
            if (auth == null) throw new MessageDeliveryException("Missing Authorization header on CONNECT");

            String subject = tokenService.getSubject(auth);
            if (subject == null) throw new MessageDeliveryException("Invalid JWT");

            Collection<? extends GrantedAuthority> authorities;
            try {
                UserDetails user = userDetailsService.loadUserByUsername(subject);
                authorities = user.getAuthorities();
            } catch (UsernameNotFoundException e) {
                authorities = List.of();
            }
            Principal principal = new UsernamePasswordAuthenticationToken(subject, "N/A", authorities);
            accessor.setUser(principal);
            return message;
        }

        if (StompCommand.SUBSCRIBE.equals(cmd) || StompCommand.SEND.equals(cmd)) {
            String dest = accessor.getDestination();
            Authentication auth = (Authentication) accessor.getUser();
            if (auth == null || !auth.isAuthenticated()) {
                throw new MessageDeliveryException("Not authenticated");
            }

            if (matchesAny(dest, "/topic/admin/**", "/queue/admin/**", "/app/admin/**")) {
                requireAnyRole(auth, "ADMIN", "ROOT");
            }

            if (matchesAny(dest, "/topic/mod/**", "/queue/mod/**", "/app/mod/**")) {
                requireAnyRole(auth, "COMMON", "ADMIN", "ROOT");
            }

            if (matchesAny(dest, "/topic/public", "/topic/public/**")) {
                return message;
            }

            if (dest != null && dest.startsWith("/user/")) {
                return message;
            }
        }

        return message;
    }

    private boolean matchesAny(String dest, String... patterns) {
        if (dest == null) return false;
        for (String p : patterns) if (matcher.match(p, dest)) return true;
        return false;
    }

    private void requireRole(Authentication auth, String role) {
        if (!hasAnyRole(auth, role)) {
            throw new MessageDeliveryException("Forbidden: requires ROLE_" + role);
        }
    }

    private void requireAnyRole(Authentication auth, String... roles) {
        if (!hasAnyRole(auth, roles)) {
            throw new MessageDeliveryException("Forbidden: requires one of " + String.join(",", roles));
        }
    }

    private boolean hasAnyRole(Authentication auth, String... roles) {
        var set = auth.getAuthorities();
        for (String r : roles) {
            String wanted = "ROLE_" + r;
            for (GrantedAuthority ga : set) {
                if (wanted.equals(ga.getAuthority())) return true;
            }
        }
        return false;
    }

    private void requireAuthority(Authentication auth, String authority) {
        boolean ok = auth.getAuthorities().stream()
                .anyMatch(a -> authority.equals(a.getAuthority()) || "ROLE_ADMIN".equals(a.getAuthority()));
        if (!ok) throw new MessageDeliveryException("Forbidden: requires " + authority);
    }
}
