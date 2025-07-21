package com.ifba.sipapi.user.api.user.service;

import com.ifba.sipapi.mail.domain.EmailData;
import com.ifba.sipapi.user.dto.UserAccountVerificationPayloadDto;
import com.ifba.sipapi.user.dto.UserPasswordRecoveryDto;
import com.ifba.sipapi.user.dto.UserUpdateDto;

public interface UserService {
    void verifyAccountWithToken(String token);
    void verifyAccount(EmailData emailData);
    void resendVerificationEmail(String email);
    void recoverPassword(String email);
    void resetPassword(UserPasswordRecoveryDto userPasswordRecoveryDto);
    void checkAndSendEmail(String email);
    void accountReactivation(String token);
    void updateUser(UserUpdateDto userUpdateDto, String email, String token);
}
