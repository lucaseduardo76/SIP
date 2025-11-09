package com.ifba.sipapi.user.api.authentication.service;

import com.ifba.sipapi.user.api.authentication.controller.AuthenticationResponseDto;
import com.ifba.sipapi.user.dto.UserAdminRegisterDto;
import com.ifba.sipapi.user.dto.UserCommomRegisterDto;
import com.ifba.sipapi.user.dto.UserLoginDto;

import java.util.Map;

public interface AuthenticationService {
    void createNewUser(UserCommomRegisterDto userCommomRegisterDto);
    AuthenticationResponseDto login(UserLoginDto userLoginDto);
    void createNewUser(UserAdminRegisterDto userAdminRegisterDto);
    AuthenticationResponseDto googleAuthentication(Map<String, String> payload);
}
