package com.ifba.sipapi.user.api.user.service;

import com.ifba.sipapi.user.dto.UserAccountVerificationPayloadDto;
import com.ifba.sipapi.user.dto.UserUpdateDto;

public interface UserService {
    void verifyAccountWithToken(String token);
    void verifyAccount(UserAccountVerificationPayloadDto userAccountVerificationPayloadDto);
    void resendVerificationEmail(String email);
    void checkAndSendEmail(String email);
    void accountReactivation(String token);
    void updateUser(UserUpdateDto userUpdateDto, String email, String token);

}
