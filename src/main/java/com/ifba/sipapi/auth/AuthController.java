package com.ifba.sipapi.auth;

import com.ifba.sipapi.auth.DTO.UserLoginResponse;
import com.ifba.sipapi.auth.DTO.UserRegisterRequest;
import com.ifba.sipapi.auth.DTO.UserRegisterResponse;
import com.ifba.sipapi.auth.services.AuthService;
import com.ifba.sipapi.auth.DTO.UserLoginRequest;
import com.ifba.sipapi.user.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {
    /*
    * TODO
    *  1- Envio de email
    *  2- Atualização da model para verificar se a conta está ativa
    *   2.1- Verificar se a conta está ativa no login e fazer comportamentos apropriados para cada caso
    *   2.2- Verificar o email já está cadastrado no registro e fazer comportamentos apriados para cada caso
    * */
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserRegisterResponse register(@RequestBody UserRegisterRequest request) {
        String message = authService.save(request);
        return new UserRegisterResponse(message);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public UserLoginResponse login(@RequestBody UserLoginRequest request) {
        return new UserLoginResponse(authService.verify(request));
    }
}
