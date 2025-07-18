package com.ifba.sipapi.user.api.user.controller;

import com.ifba.sipapi.docs.swagger.AuthenticationAPIDocs;
import com.ifba.sipapi.docs.swagger.UserAPIDocs;
import com.ifba.sipapi.user.dto.UserAccountVerificationPayloadDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Tag(name = "UserApi", description = "Controle responsavel pelas requisições do usuario.")
public interface UserApi {

    @UserAPIDocs.VerifyAccount
    @PostMapping("/verify-account/{token}")
    @ResponseStatus(HttpStatus.OK)
    void verifyWithToken(@PathVariable String token);

    @UserAPIDocs.VerifyAccount
    @PostMapping("/verify-account")
    @ResponseStatus(HttpStatus.OK)
    void verify(@RequestBody @Valid UserAccountVerificationPayloadDto userAccountVerificationPayloadDto);

    @UserAPIDocs.VerifyAccount
    @PostMapping("/resend-verify-account")
    @ResponseStatus(HttpStatus.OK)
    void resendVerification(@RequestBody String email);
}
