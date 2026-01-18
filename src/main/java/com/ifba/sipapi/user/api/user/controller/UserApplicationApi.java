package com.ifba.sipapi.user.api.user.controller;

import com.ifba.sipapi.user.api.user.service.UserApplicationService;
import com.ifba.sipapi.user.dto.UserDetailsResponseDto;
import com.ifba.sipapi.user.dto.UserPasswordRecoveryDto;
import com.ifba.sipapi.user.dto.UserPasswordUpdateDto;
import com.ifba.sipapi.user.dto.UserUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Log4j2
public class UserApplicationApi implements  UserApi{

    private final UserApplicationService userService;

    @Override
    public void verifyWithToken(String token) {
        log.info("[start] UserApplicationApi - verifyWithToken");
        userService.verifyAccountWithToken(token);
        log.debug("[finish] UserApplicationApi - verifyWithToken");
    }

    @Override
    public void resendVerification(String email){
        log.info("[start] UserApplicationApi - resendVerification");
        userService.resendVerificationEmail(email);
        log.debug("[finish] UserApplicationApi - resendVerification");
    }

    @Override
    public void requestReactivation(String email) {
        log.info("[start] UserApplicationApi - requestReactivation");
        userService.checkAndSendEmail(email);
        log.debug("[finish] UserApplicationApi - requestReactivation");
    }

    @Override
    public void reactivateAccount(String token) {
        log.info("[start] UserApplicationApi - reactivateAccount");
        userService.accountReactivation(token);
        log.debug("[finish] UserApplicationApi - reactivateAccount");
    }

    @Override
    public void update(String token, String email, UserUpdateDto userUpdateDto) {
        log.info("[start] UserApplicationApi - update");
        userService.updateUser(userUpdateDto, email, token);
        log.debug("[finish] UserApplicationApi - update");
    }

    @Override
    public void updateAdminUserByRoot(String email, UserUpdateDto userUpdateDto) {
        log.info("[start] UserApplicationApi - updateAdminUserByRoot");
        userService.updateUserByRoot(userUpdateDto, email);
        log.debug("[finish] UserApplicationApi - updateAdminUserByRoot");
    }

    @Override
    public void resetPassword(UserPasswordRecoveryDto userPasswordRecoveryDto) {
        log.info("[start] UserApplicationApi - resetPassword");
        userService.resetPassword(userPasswordRecoveryDto);
        log.debug("[finish] UserApplicationApi - resetPassword");
    }

    @Override
    public void recoverPassword(String email) {
        log.info("[start] UserApplicationApi - recoverPassword");
        userService.recoverPassword(email);
        log.debug("[finish] UserApplicationApi - recoverPassword");
    }

    @Override
    public void updatePassword(String token, String email, UserPasswordUpdateDto userPasswordUpdateDto){
        log.info("[start] UserApplicationApi - updatePassword");
        userService.updatePassword(email, userPasswordUpdateDto, token);
        log.debug("[finish] UserApplicationApi - updatePassword");
    }

    @Override
    public void updateProfileImageWithOutLogin(String email, MultipartFile profileImage) {
        log.info("[start] UserApplicationApi - updateProfileImageWithOutLogin");
        userService.updateProfileImage(profileImage, null, email);
        log.debug("[finish] UserApplicationApi - updateProfileImageWithOutLogin");
    }

    @Override
    public void updateProfileImage(String token, String email, MultipartFile profileImage) {
        log.info("[start] UserApplicationApi - updateProfileImage");
        userService.updateProfileImage(profileImage, token, email);
        log.debug("[finish] UserApplicationApi - updateProfileImage");
    }

    @Override
    public void updateProfileImageByRoot(String email, MultipartFile profileImage) {
        log.info("[start] UserApplicationApi - updateProfileImageByRoot");
        userService.updateProfileImageByRoot(profileImage, email);
        log.debug("[finish] UserApplicationApi - updateProfileImageByRoot");
    }

    @Override
    public UserDetailsResponseDto userDetails(String token, String email) {
        log.info("[start] UserApplicationApi - userDetails");
        UserDetailsResponseDto userDetailsResponse = userService.getUserDetails(email, token);
        log.debug("[finish] UserApplicationApi - userDetails");
        return userDetailsResponse;
    };

    @Override
    public List<UserDetailsResponseDto> adminUsers(String token, String email) {
        log.info("[start] UserApplicationApi - adminUsers");
        List<UserDetailsResponseDto> userAdminsResponseDtos = userService.getUserAdmins(email, token);
        log.debug("[finish] UserApplicationApi - adminUsers");
        return userAdminsResponseDtos;
    }

    @Override
    public void deleteAdmin(String email) {
        log.info("[start] UserApplicationApi - deleteAdmin");
        userService.deleteAdmin(email);
        log.debug("[finish] UserApplicationApi - deleteAdmin");
    }

    @Override
    public UserDetailsResponseDto adminUserDetail(String email) {
        log.info("[start] UserApplicationApi - adminUserDetail");
        UserDetailsResponseDto userAdminsResponseDto = userService.getAdminDetail(email);
        log.debug("[finish] UserApplicationApi - adminUserDetail");
        return userAdminsResponseDto;
    }
}
