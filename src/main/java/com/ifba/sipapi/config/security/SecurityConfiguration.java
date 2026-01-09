package com.ifba.sipapi.config.security;


import com.ifba.sipapi.config.websocket.JwtWsPreAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final SecurityFilter securityFilter;
    private final JwtWsPreAuthFilter wsFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(c -> {
                })
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "public/**",
                                "v3/api-docs/**",
                                "swagger-ui/**",
                                "swagger-ui.html",
                                "v3/api-docs/swagger-config",
                                "v3/api-docs",
                                "user/account/**",
                                "oauth2/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "authentication/register-admin").hasRole("ROOT")
                        .requestMatchers(HttpMethod.POST, "items/admin/**").hasRole("ADMIN")
                        .requestMatchers("user/root/**").hasRole("ROOT")
                        .requestMatchers("items/root/**").hasRole("ROOT")
                        .requestMatchers(HttpMethod.POST, "authentication/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/authentication/oauth2/success", true)
                        .failureUrl("/authentication/oauth2/failure")
                )
                .addFilterBefore(wsFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
