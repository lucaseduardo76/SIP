package com.ifba.sipapi.user.api.authentication.service;

import com.ifba.sipapi.user.dto.UserCommomRegisterDto;

public interface AuthenticationService {
    void createNewUser(UserCommomRegisterDto userCommomRegisterDto);
}
