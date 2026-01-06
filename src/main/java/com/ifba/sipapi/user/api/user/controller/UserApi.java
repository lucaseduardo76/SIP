package com.ifba.sipapi.user.api.user.controller;

import com.ifba.sipapi.docs.swagger.UserAPIDocs;
import com.ifba.sipapi.user.dto.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/user")
@Tag(name = "UserApi", description = "Controle responsavel pelas requisições do usuario.")
public interface UserApi {

    @UserAPIDocs.VerifyAccount
    @PostMapping("/account/verify/{token}")
    @ResponseStatus(HttpStatus.OK)
    void verifyWithToken(@PathVariable String token);

    @UserAPIDocs.VerifyAccount
    @PostMapping("/account/resend-verify-account/{email}")
    @ResponseStatus(HttpStatus.OK)
    void resendVerification(@PathVariable String email);

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
    void resetPassword(@RequestBody @Valid UserPasswordRecoveryDto userPasswordRecoveryDto);

    @UserAPIDocs.RecoverPassword
    @PostMapping("/account/password-recovery/{email}")
    @ResponseStatus(HttpStatus.OK)
    void recoverPassword(@PathVariable String email);

    @UserAPIDocs.Update
    @PutMapping("/update/{email}")
    @ResponseStatus(HttpStatus.OK)
    void update(
            @RequestHeader(name = "Authorization", required = true) String token,
            @PathVariable String email,
            @RequestBody @Valid UserUpdateDto userUpdateDto);

    @UserAPIDocs.Update
    @PutMapping("/root/update/{email}")
    @ResponseStatus(HttpStatus.OK)
    void updateAdminUserByRoot(
            @PathVariable String email,
            @RequestBody @Valid UserUpdateDto userUpdateDto);

    @UserAPIDocs.UpdatePassword
    @PatchMapping("/update-password/{email}")
    @ResponseStatus(HttpStatus.OK)
    void updatePassword(
            @RequestHeader(name = "Authorization", required = true) String token,
            @PathVariable String email,
            @RequestBody @Valid UserPasswordUpdateDto userPasswordUpdateDto);

    @UserAPIDocs.UpdateProfileImage
    @PatchMapping("/update-profile/{email}")
    @ResponseStatus(HttpStatus.OK)
    void updateProfileImage(
            @RequestHeader(name = "Authorization", required = true) String token,
            @PathVariable String email,
            @RequestParam("profileImage") MultipartFile profileImage);

    @UserAPIDocs.UpdateProfileImageByRoot
    @PatchMapping("/root/update-profile/{email}")
    @ResponseStatus(HttpStatus.OK)
    void updateProfileImageByRoot(
            @PathVariable String email,
            @RequestParam("profileImage") MultipartFile profileImage);

    @UserAPIDocs.AdminUsers
    @GetMapping("/root/admin-users/{email}")
    @ResponseStatus(HttpStatus.OK)
    List<UserDetailsResponseDto> adminUsers(
            @RequestHeader(name = "Authorization", required = true) String token,
            @PathVariable String email);

    @UserAPIDocs.DeleteAdmin
    @DeleteMapping("/root/delete-admin/{email}")
    @ResponseStatus(HttpStatus.OK)
    void deleteAdmin(
            @PathVariable String email);

    @UserAPIDocs.AdminDetail
    @GetMapping("/root/admin-detail/{email}")
    @ResponseStatus(HttpStatus.OK)
    UserDetailsResponseDto adminUserDetail(
            @PathVariable String email);

    @UserAPIDocs.UserDetails
    @GetMapping("/user-details/{email}")
    @ResponseStatus(HttpStatus.OK)
    UserDetailsResponseDto userDetails(
            @RequestHeader(name = "Authorization", required = true) String token,
            @PathVariable String email
    );
}
