package com.ifba.sipapi.user.api.user.service;

import com.ifba.sipapi.mail.domain.EmailData;
import com.ifba.sipapi.user.dto.UserAccountVerificationPayloadDto;
import com.ifba.sipapi.user.dto.UserPasswordRecoveryDto;
import com.ifba.sipapi.user.dto.UserPasswordUpdateDto;
import com.ifba.sipapi.user.dto.UserUpdateDto;
import org.springframework.web.multipart.MultipartFile;
import com.ifba.sipapi.user.dto.*;

public interface UserService {
    void verifyAccountWithToken(String token);
    void verifyAccount(EmailData emailData);
    void resendVerificationEmail(String email);
    void recoverPassword(String email);
    void resetPassword(UserPasswordRecoveryDto userPasswordRecoveryDto);
    void checkAndSendEmail(String email);
    void accountReactivation(String token);
    void updateUser(UserUpdateDto userUpdateDto, String email, String token);
    void updatePassword(String email, UserPasswordUpdateDto userPasswordUpdateDto, String token);
    void updateProfileImage(MultipartFile profileImage, String token, String email);
    UserDetailsResponseDto getUserDetails(String email, String token);
}
