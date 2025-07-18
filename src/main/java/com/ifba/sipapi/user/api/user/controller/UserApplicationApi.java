package com.ifba.sipapi.user.api.user.controller;

import com.ifba.sipapi.user.api.user.service.UserApplicationService;
import com.ifba.sipapi.user.dto.UserAccountVerificationPayloadDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
public class UserApplicationApi implements  UserApi{

    private final UserApplicationService userService;

    @Override
    public void verifyWithToken(String token) {
        log.info("[start] AuthenticationApplicationApi - verifyWithToken");
        userService.verifyAccountWithToken(token);
        log.debug("[finish] AuthenticationApplicationApi - verifyWithToken");
    }

    @Override
    public void verify(UserAccountVerificationPayloadDto userAccountVerificationPayloadDto){
        log.info("[start] AuthenticationApplicationApi - verify");
        userService.verifyAccount(userAccountVerificationPayloadDto);
        log.debug("[finish] AuthenticationApplicationApi - verify");
    }

    @Override
    public void resendVerification(String email){
        log.info("[start] AuthenticationApplicationApi - resendVerification");
        userService.resendVerificationEmail(email);
        log.debug("[finish] AuthenticationApplicationApi - resendVerification");
    }

}
