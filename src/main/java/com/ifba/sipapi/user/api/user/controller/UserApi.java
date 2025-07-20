package com.ifba.sipapi.user.api.user.controller;

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
    @PostMapping("/account/verify/{token}")
    @ResponseStatus(HttpStatus.OK)
    void verifyWithToken(@PathVariable String token);

    @UserAPIDocs.VerifyAccount
    @PostMapping("/account/verify")
    @ResponseStatus(HttpStatus.OK)
    void verify(@RequestBody @Valid UserAccountVerificationPayloadDto userAccountVerificationPayloadDto);

    @UserAPIDocs.VerifyAccount
    @PostMapping("/account/resend-verification")
    @ResponseStatus(HttpStatus.OK)
    void resendVerification(@RequestBody String email);

    @UserAPIDocs.RequestAccountReactivation
    @PostMapping("/account/request-reactivation/{email}")
    @ResponseStatus(HttpStatus.OK)
    void requestReactivation(@PathVariable String email);

    @UserAPIDocs.ReactivateAccount
    @PostMapping("/account/reactivate/{token}")
    @ResponseStatus(HttpStatus.OK)
    void reactivateAccount(@PathVariable String token);
}
