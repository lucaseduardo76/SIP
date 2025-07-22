package com.ifba.sipapi.user.api.user.controller;

import com.ifba.sipapi.user.api.user.service.UserApplicationService;
import com.ifba.sipapi.user.dto.UserPasswordRecoveryDto;
import com.ifba.sipapi.user.dto.UserPasswordUpdateDto;
import com.ifba.sipapi.user.dto.UserUpdateDto;
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

    @Override
    public void update(String token, String email, UserUpdateDto userUpdateDto) {
        log.info("[start] UserApplicationApi - update");
        userService.updateUser(userUpdateDto, email, token);
        log.debug("[finish] UserApplicationApi - update");
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

    @Override
    public void updatePassword(String userId, UserPasswordUpdateDto userPasswordUpdateDto){
        log.info("[start] UserApplicationApi - updatePassword");
        userService.updatePassword(userId, userPasswordUpdateDto);
        log.debug("[finish] UserApplicationApi - updatePassword");
    }
}
