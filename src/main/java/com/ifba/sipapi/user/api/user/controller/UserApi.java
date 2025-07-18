package com.ifba.sipapi.user.api.user.controller;

import com.ifba.sipapi.docs.swagger.AuthenticationAPIDocs;
import com.ifba.sipapi.docs.swagger.UserAPIDocs;
import com.ifba.sipapi.mail.domain.EmailData;
import com.ifba.sipapi.user.dto.UserAccountVerificationPayloadDto;
import com.ifba.sipapi.user.dto.UserPasswordRecoveryDto;
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
    @PostMapping("/resend-verify-account")
    @ResponseStatus(HttpStatus.OK)
    void resendVerification(@RequestBody String email);

    @UserAPIDocs.RecoverPassword
    @PostMapping("/password-reset")
    @ResponseStatus(HttpStatus.OK)
    void resetPassword(@RequestBody UserPasswordRecoveryDto userPasswordRecoveryDto);

    @UserAPIDocs.RecoverPassword
    @PostMapping("/password-recovery")
    @ResponseStatus(HttpStatus.OK)
    void recoverPassword(@RequestBody String email);
}
