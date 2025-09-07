package com.ifba.sipapi.config.websocket;

import com.ifba.sipapi.config.security.TokenService;
import com.ifba.sipapi.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest; // servlet stack
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class CustomWebSocketHandshakeHandler extends DefaultHandshakeHandler {
    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        final var token = getQueryParam(request, "access_token")
                .map(t -> URLDecoder.decode(t, StandardCharsets.UTF_8))
                .orElseThrow(() -> new IllegalArgumentException("access_token is required"));

        final var username = tokenService.validateToken(token);
        final var userDetails = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        final var roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toSet());

        final var path = request.getURI().getPath();
        final String channel = path.endsWith("/admin") ? "admin" : "common";
        if (path.endsWith("/admin") && !roles.contains("ROLE_ADMIN")) {
            throw new AccessDeniedException("Admin only");
        }

        attributes.put("roles", roles);
        attributes.put("username", username);
        attributes.put("channel", channel);

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    public static Optional<String> getQueryParam(final ServerHttpRequest request, final String paramName) {
        URI uri = request.getURI();
        String query = uri.getQuery();

        if (query != null) {
            String[] params = query.split("&");
            Map<String, String> queryParams = Arrays.stream(params)
                    .map(param -> param.split("=", 2))
                    .collect(Collectors.toMap(p -> p[0], p -> p.length > 1 ? p[1] : ""));

            return Optional.ofNullable(queryParams.get(paramName));
        }

        return Optional.empty();
    }
}
