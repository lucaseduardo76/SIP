package com.ifba.sipapi.user.api.authentication.controller;

import com.ifba.sipapi.docs.swagger.AuthenticationAPIDocs;
import com.ifba.sipapi.user.dto.UserAdminRegisterDto;
import com.ifba.sipapi.user.dto.UserCommomRegisterDto;
import com.ifba.sipapi.user.dto.UserLoginDto;
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

    @AuthenticationAPIDocs.RegisterAdmin
    @PostMapping("/register-admin")
    @ResponseStatus(HttpStatus.CREATED)
    void registerAdmin(@RequestBody @Valid UserAdminRegisterDto userAdminRegisterDto);

    @AuthenticationAPIDocs.Login
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    AuthenticationResponseDto login(@RequestBody @Valid UserLoginDto userLoginDto);

    @AuthenticationAPIDocs.TokenTeste
    @GetMapping("/token-teste")
    @ResponseStatus(code = HttpStatus.OK)
    Map<String, String> tokenTeste();

    @AuthenticationAPIDocs.Login
    @PostMapping("/google")
    AuthenticationResponseDto authenticateWithGoogle(@RequestBody Map<String, String> payload);
}
