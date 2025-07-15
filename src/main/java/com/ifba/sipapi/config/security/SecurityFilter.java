package com.ifba.sipapi.config.security;


import com.ifba.sipapi.user.infra.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Log4j2
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        try {
            var token = this.recoverToken(request);
            if (token != null) {
                var username = tokenService.validateToken(token);
                var userDetails = userRepository.findByEmail(username)
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

                var authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        } catch (RuntimeException e) {
//            response.setStatus(HttpServletResponse.SC_FORBIDDEN);response.setContentType("application/json");
//            var errorResponse = ErrorApiResponse.builder()
//                    .message("Usuário não encontrado ou token inválido.")
//                    .description("Faça login novamente ou busque ajuda do suporte.")
//                    .build();
//            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
//            var json = mapper.writeValueAsString(errorResponse);
//
//            response.getWriter().write(json);

            log.error("Aguardando criação das exceptions...");

        }
    }

    private String recoverToken(HttpServletRequest request){
        var authHeader = request.getHeader("Authorization");
        if(authHeader == null) return null;
        return authHeader.replace("Bearer ", "");
    }

}

