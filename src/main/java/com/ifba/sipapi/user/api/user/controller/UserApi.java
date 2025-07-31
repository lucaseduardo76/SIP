package com.ifba.sipapi.user.api.user.controller;

import com.ifba.sipapi.docs.swagger.UserAPIDocs;
import com.ifba.sipapi.user.dto.*;
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
    @PostMapping("/account/resend-verify-account")
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

    @UserAPIDocs.RecoverPassword
    @PostMapping("/account/password-reset")
    @ResponseStatus(HttpStatus.OK)
    void resetPassword(@RequestBody UserPasswordRecoveryDto userPasswordRecoveryDto);

    @UserAPIDocs.RecoverPassword
    @PostMapping("/account/password-recovery")
    @ResponseStatus(HttpStatus.OK)
    void recoverPassword(@RequestBody String email);

    @UserAPIDocs.Update
    @PutMapping("/update/{email}")
    @ResponseStatus(HttpStatus.OK)
    void update(
            @RequestHeader(name = "Authorization", required = true) String token,
            @PathVariable String email,
            @RequestBody @Valid UserUpdateDto userUpdateDto);

    @UserAPIDocs.UpdatePassword
    @PutMapping("/update-password/{email}")
    @ResponseStatus(HttpStatus.OK)
    void updatePassword(
            @RequestHeader(name = "Authorization", required = true) String token,
            @PathVariable String email,
            @RequestBody @Valid UserPasswordUpdateDto userPasswordUpdateDto);

    @UserAPIDocs.UserDetails
    @GetMapping("/user-details/{email}")
    @ResponseStatus(HttpStatus.OK)
    UserDetailsResponseDto userDetails(
            @RequestHeader(name = "Authorization", required = true) String token,
            @PathVariable String email
    );
}
