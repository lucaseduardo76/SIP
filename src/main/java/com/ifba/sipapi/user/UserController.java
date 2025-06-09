package com.ifba.sipapi.user;

import com.ifba.sipapi.user.DTO.UserRegisterRequest;
import com.ifba.sipapi.user.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserModel register(@RequestBody UserRegisterRequest request) {
        return userService.save(request);
    }
}
