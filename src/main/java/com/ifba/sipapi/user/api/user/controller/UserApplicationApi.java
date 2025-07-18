package com.ifba.sipapi.user.api.user.controller;

import com.ifba.sipapi.mail.domain.EmailData;
import com.ifba.sipapi.user.api.user.service.UserApplicationService;
import com.ifba.sipapi.user.dto.UserAccountVerificationPayloadDto;
import com.ifba.sipapi.user.dto.UserPasswordRecoveryDto;
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
    public void resendVerification(String email){
        log.info("[start] AuthenticationApplicationApi - resendVerification");
        userService.resendVerificationEmail(email);
        log.debug("[finish] AuthenticationApplicationApi - resendVerification");
    }

    @Override
    public void resetPassword(UserPasswordRecoveryDto userPasswordRecoveryDto) {
        log.info("[start] UserApplicationApi - resetPassword");
        userService.resetPassword(userPasswordRecoveryDto);
        log.debug("[finish] UserApplicationApi - resetPassword");
    }

    @Override
    public void recoverPassword(String email) {
        log.info("[start] UserApplicationApi - recoverPassword");
        userService.recoverPassword(email);
        log.debug("[finish] UserApplicationApi - recoverPassword");
    }
}
