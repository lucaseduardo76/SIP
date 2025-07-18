package com.ifba.sipapi.user.api.user.service;

import com.ifba.sipapi.mail.domain.EmailData;
import com.ifba.sipapi.user.dto.UserAccountVerificationPayloadDto;
import com.ifba.sipapi.user.dto.UserPasswordRecoveryDto;

public interface UserService {
    void verifyAccountWithToken(String token);
    void verifyAccount(EmailData emailData);
    void resendVerificationEmail(String email);
    void recoverPassword(String email);
    void resetPassword(UserPasswordRecoveryDto userPasswordRecoveryDto);
}
