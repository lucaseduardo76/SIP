package com.ifba.sipapi.user.api.authentication.service;

import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.user.dto.UserCommomRegisterDto;
import com.ifba.sipapi.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthenticationApplicationService implements AuthenticationService {
    private final UserRepository userRepository;

    @Override
    public void createNewUser(UserCommomRegisterDto userCommomRegisterDto) {
        log.info("[start] AuthenticationApplicationService - createNewUser");
        userRepository.save(new User(userCommomRegisterDto));
        log.debug("[finish] AuthenticationApplicationService - createNewUser");
    }
}
