package com.ifba.sipapi.user.api.authentication.controller;

import com.ifba.sipapi.user.dto.UserLoginDto;
import com.ifba.sipapi.user.dto.UserCommomRegisterDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/authentication")
public interface AuthenticationApi {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody @Valid UserCommomRegisterDto userCommomRegisterDto);

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthenticationResponseDto login(UserLoginDto userLoginDto);

}
