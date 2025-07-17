package com.ifba.sipapi.user.api.authentication.service;

import com.ifba.sipapi.user.api.authentication.controller.AuthenticationResponseDto;
import com.ifba.sipapi.user.dto.UserAccountVerificationPayloadDto;
import com.ifba.sipapi.user.dto.UserCommomRegisterDto;
import com.ifba.sipapi.user.dto.UserLoginDto;

public interface AuthenticationService {
    void createNewUser(UserCommomRegisterDto userCommomRegisterDto);
    AuthenticationResponseDto login(UserLoginDto userLoginDto);
    void verifyAccountWithToken(String token);
    void verifyAccount(UserAccountVerificationPayloadDto userAccountVerificationPayloadDto);
    void resendVerificationEmail(String email);
}
