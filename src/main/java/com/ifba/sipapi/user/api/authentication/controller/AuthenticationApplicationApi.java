package com.ifba.sipapi.user.api.authentication.controller;

import com.ifba.sipapi.user.api.authentication.service.AuthenticationService;
import com.ifba.sipapi.user.dto.UserLoginDto;
import com.ifba.sipapi.user.dto.UserCommomRegisterDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
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
        log.info("[start] AuthenticationApplicationApi - login");
        AuthenticationResponseDto authenticationResponse = authenticationService.login(userLoginDto);
        log.debug("[finish] AuthenticationApplicationApi - login");
        return authenticationResponse;
    }

    @Override
    public Map<String, String> tokenTeste() {
        log.info("[start] AuthenticationAPI - tokenTeste");
        String message = "Token valido!";
        log.debug("[finish] AuthenticationAPI - tokenTeste");
        return Map.of("message", message);
    }
}
