package com.ifba.sipapi.user.api.authentication.controller;

import com.ifba.sipapi.user.api.authentication.service.AuthenticationService;
import com.ifba.sipapi.user.dto.UserLoginDto;
import com.ifba.sipapi.user.dto.UserCommomRegisterDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus
@RequestMapping("/authentication")
@RequiredArgsConstructor
@Log4j2
public class AuthenticationApplicationApi implements AuthenticationApi {

    private final AuthenticationService authenticationService;

    @Override
    public void register(UserCommomRegisterDto userCommomRegisterDto) {
       log.info("[start] AuthenticationApplicationApi - register");
       authenticationService.createNewUser(userCommomRegisterDto);
       log.debug("[finish] AuthenticationApplicationApi - register");
    }

    @Override
    public AuthenticationResponseDto login(UserLoginDto userLoginDto) {
        return null;
    }
}
