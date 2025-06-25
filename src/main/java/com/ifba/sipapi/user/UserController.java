package com.ifba.sipapi.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/test")
    @ResponseStatus(HttpStatus.OK)
    public String register() {
        return "test";
    }
}
