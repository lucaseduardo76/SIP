package com.ifba.sipapi.config.root;

import com.ifba.sipapi.user.domain.Role;
import com.ifba.sipapi.user.domain.StatusMember;
import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.user.dto.UserRootRegisterDto;
import com.ifba.sipapi.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class RootUserInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RootProperties rootProps;

    @Bean
    public CommandLineRunner initRootUser() {
        return args -> {
            log.info("[start] RootUserInitializer - initRootUser");
            if (userRepository.findByEmail(rootProps.getEmail()).isEmpty()) {
                UserRootRegisterDto dto = new UserRootRegisterDto(
                        rootProps.getName(),
                        rootProps.getCpf(),
                        rootProps.getEmail(),
                        rootProps.getPassword(),
                        rootProps.getPhone()
                );

                dto.updateHashedPassword(passwordEncoder.encode(dto.getPassword()));
                User rootUser = new User(dto, Role.ROOT);
                userRepository.save(rootUser);
            }
            log.debug("[finish] RootUserInitializer - initRootUser");
        };
    }
}

