package com.ifba.sipapi.auth;

import com.ifba.sipapi.auth.DTO.UserRegisterRequest;
import com.ifba.sipapi.auth.services.AuthService;
import com.ifba.sipapi.user.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserModel register(@RequestBody UserRegisterRequest request) {
        return authService.save(request);
    }
}
