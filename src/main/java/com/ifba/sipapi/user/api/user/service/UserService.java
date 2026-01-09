package com.ifba.sipapi.user.api.user.service;

import com.ifba.sipapi.mail.domain.EmailData;
import com.ifba.sipapi.user.dto.UserPasswordRecoveryDto;
import com.ifba.sipapi.user.dto.UserPasswordUpdateDto;
import com.ifba.sipapi.user.dto.UserUpdateDto;
import org.springframework.web.multipart.MultipartFile;
import com.ifba.sipapi.user.dto.*;

import java.util.List;

public interface UserService {
    void verifyAccountWithToken(String token);
    void verifyAccount(EmailData emailData);
    void resendVerificationEmail(String email);
    void recoverPassword(String email);
    void resetPassword(UserPasswordRecoveryDto userPasswordRecoveryDto);
    void checkAndSendEmail(String email);
    void accountReactivation(String token);
    void updateUser(UserUpdateDto userUpdateDto, String email, String token);
    void updateUserByRoot(UserUpdateDto userUpdateDto, String email);
    void updatePassword(String email, UserPasswordUpdateDto userPasswordUpdateDto, String token);
    void updateProfileImage(MultipartFile profileImage, String token, String email);
    void updateProfileImageByRoot(MultipartFile profileImage, String email);
    void deleteAdmin(String email);
    UserDetailsResponseDto getUserDetails(String email, String token);
    List<UserDetailsResponseDto> getUserAdmins(String email, String token);
    UserDetailsResponseDto getAdminDetail(String email);
}
