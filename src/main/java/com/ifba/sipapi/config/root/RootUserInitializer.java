package com.ifba.sipapi.config.root;

import com.ifba.sipapi.user.domain.Role;
import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.user.dto.UserRootRegisterDto;
import com.ifba.sipapi.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

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

            Assert.hasText(rootProps.getEmail(), "ROOT email must not be empty");
            Assert.hasText(rootProps.getPassword(), "ROOT password must not be empty");
            Assert.hasText(rootProps.getName(), "ROOT name must not be empty");
            Assert.hasText(rootProps.getCpf(), "ROOT CPF must not be empty");
            Assert.hasText(rootProps.getPhone(), "ROOT phone must not be empty");

            long rootCount = userRepository.countByRole(Role.ROOT);
            boolean rootEmailExists = userRepository.findByEmail(rootProps.getEmail()).isPresent();

            if (!rootEmailExists || rootCount > 1) {
                log.warn("Inconsistent ROOT user state detected (rootCount = {}, emailExists = {}). Resetting ROOT user...", rootCount, rootEmailExists);
                userRepository.deleteAllByRole(Role.ROOT);
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

