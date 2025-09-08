package com.ifba.sipapi.config.websocket;

import com.ifba.sipapi.config.security.TokenService;
import com.ifba.sipapi.user.infra.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtWsPreAuthFilter extends OncePerRequestFilter {
    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        return !req.getRequestURI().startsWith("/sip/api/ws");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        try {
            var token = Optional.ofNullable(req.getParameter("access_token"))
                    .map(URLDecoder::decode)
                    .orElse(null);

            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var username = tokenService.validateToken(token);
                var user = userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException(username));
                var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "WS auth failed");
            return;
        }
        chain.doFilter(req, res);
    }
}

