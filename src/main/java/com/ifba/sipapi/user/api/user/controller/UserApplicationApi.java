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
        log.info("[start] UserApplicationApi - verifyWithToken");
        userService.verifyAccountWithToken(token);
        log.debug("[finish] UserApplicationApi - verifyWithToken");
    }

    @Override
    public void verify(UserAccountVerificationPayloadDto userAccountVerificationPayloadDto){
        log.info("[start] UserApplicationApi - verify");
        userService.verifyAccount(userAccountVerificationPayloadDto);
        log.debug("[finish] UserApplicationApi - verify");
    }

    @Override
    public void resendVerification(String email){
        log.info("[start] UserApplicationApi - resendVerification");
        userService.resendVerificationEmail(email);
        log.debug("[finish] UserApplicationApi - resendVerification");
    }

    @Override
    public void requestReactivation(String email) {
        log.info("[start] UserApplicationApi - requestReactivation");
        userService.checkAndSendEmail(email);
        log.debug("[finish] UserApplicationApi - requestReactivation");
    }

    @Override
    public void reactivateAccount(String token) {
        log.info("[start] UserApplicationApi - reactivateAccount");
        userService.accountReactivation(token);
        log.debug("[finish] UserApplicationApi - reactivateAccount");
    }

}
