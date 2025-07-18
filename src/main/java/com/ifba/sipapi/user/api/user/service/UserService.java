package com.ifba.sipapi.user.api.user.service;

import com.ifba.sipapi.user.dto.UserAccountVerificationPayloadDto;

public interface UserService {
    void verifyAccountWithToken(String token);
    void verifyAccount(UserAccountVerificationPayloadDto userAccountVerificationPayloadDto);
    void resendVerificationEmail(String email);
}
