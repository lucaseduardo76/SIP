package com.ifba.sipapi.user.api.authentication.controller;

import com.ifba.sipapi.user.dto.UserAccountVerificationPayloadDto;
import com.ifba.sipapi.user.dto.UserLoginDto;
import com.ifba.sipapi.user.dto.UserCommomRegisterDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RequestMapping("authentication")
public interface AuthenticationApi {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    void register(@RequestBody @Valid UserCommomRegisterDto userCommomRegisterDto);

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    AuthenticationResponseDto login(@RequestBody @Valid UserLoginDto userLoginDto);

    @PostMapping("/verify-account/{token}")
    @ResponseStatus(HttpStatus.OK)
    void verifyWithToken(@PathVariable String token);

    @PostMapping("/verify-account")
    @ResponseStatus(HttpStatus.OK)
    void verify(@RequestBody @Valid UserAccountVerificationPayloadDto userAccountVerificationPayloadDto);
}
