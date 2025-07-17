package com.ifba.sipapi.user.api.authentication.controller;

import com.ifba.sipapi.docs.swagger.AuthenticationAPIDocs;
import com.ifba.sipapi.user.dto.UserAccountVerificationPayloadDto;
import com.ifba.sipapi.user.dto.UserLoginDto;
import com.ifba.sipapi.user.dto.UserCommomRegisterDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RequestMapping("authentication")
@Tag(name = "AuthenticationAPI", description = "Controle responsavel pela autenticação do usuarios.")
public interface AuthenticationApi {

    @AuthenticationAPIDocs.Register
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    void register(@RequestBody @Valid UserCommomRegisterDto userCommomRegisterDto);

    @AuthenticationAPIDocs.Login
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    AuthenticationResponseDto login(@RequestBody @Valid UserLoginDto userLoginDto);

    @AuthenticationAPIDocs.tokenTeste
    @GetMapping("/token-teste")
    @ResponseStatus(code = HttpStatus.OK)
    public Map<String, String> tokenTeste();

    @AuthenticationAPIDocs.VerifyAccount
    @PostMapping("/verify-account/{token}")
    @ResponseStatus(HttpStatus.OK)
    void verifyWithToken(@PathVariable String token);

    @AuthenticationAPIDocs.VerifyAccount
    @PostMapping("/verify-account")
    @ResponseStatus(HttpStatus.OK)
    void verify(@RequestBody @Valid UserAccountVerificationPayloadDto userAccountVerificationPayloadDto);
}
