package com.ifba.sipapi.user.api.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.config.security.TokenService;
import com.ifba.sipapi.mail.domain.EmailData;
import com.ifba.sipapi.mail.domain.EmailSender;
import com.ifba.sipapi.mail.domain.EmailType;
import com.ifba.sipapi.mail.infra.KafkaApplicationEmailProducer;
import com.ifba.sipapi.user.domain.StatusMember;
import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.user.dto.*;
import com.ifba.sipapi.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.support.LogIfLevelEnabled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserApplicationService implements UserService {
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final KafkaApplicationEmailProducer kafkaApplicationEmailProducer;

    @Override
    public void verifyAccountWithToken(String token) {
        log.info("[start] verifyAccountWithToken");
        EmailData emailData = extractPayloadFromToken(token);
        log.info(emailData.to());
        verifyAccount(emailData);
        log.debug("[finish] verifyAccountWithToken");
    }

    @Override
    public void verifyAccount(EmailData emailData) {
        log.info("[start] verifyAccount");
        User user = userRepository.findByEmail(emailData.to())
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        user.handleAccountVerification(emailData.code());
        userRepository.save(user);
        log.debug("[finish] verifyAccount");
    }

    @Override
    public void resendVerificationEmail(String email) {
        log.info("[start] AuthenticationApplicationService - resendVerificationEmail");
        User user = userRepository.findByEmail(email)
                .filter(u -> u.getStatusMember().equals(StatusMember.NOT_VERIFIED))
                .orElseThrow(() -> APIException.build(HttpStatus.BAD_REQUEST, "Usuário ja foi verificado ou email não encontrado!"));
        sendEmail(user.getEmail(), EmailType.VERIFICATION);
        log.debug("[finish] AuthenticationApplicationService - resendVerificationEmail");
    }

    @Override
    public void recoverPassword(String email) {
        log.info("[start] UserApplicationService - recoverPassword");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        user.updatePasswordRecoveryCode();
        userRepository.save(user);
        this.sendEmail(email, EmailType.RECOVER_PASSWORD);
        log.debug("[finish] UserApplicationService - recoverPassword");
    }

    @Override
    public void resetPassword(UserPasswordRecoveryDto userPasswordRecoveryDto) {
        log.info("[start] UserApplicationService - resetPassword");
        EmailData payload = extractPayloadFromToken(userPasswordRecoveryDto.getToken());
        User user = userRepository.findByEmail(payload.to())
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        user.resetPassword(payload.code(), passwordEncoder.encode(userPasswordRecoveryDto.getPassword()));
        userRepository.save(user);
        log.debug("[finish] UserApplicationService - resetPassword");
    }

    @Override
    public void checkAndSendEmail(String email) {
        log.info("[start] AuthenticationApplicationService - checkAndSendEmail");
        User user = userRepository.findByEmail(email)
                .filter(u -> u.getStatusMember().equals(StatusMember.BLOCKED))
                .orElseThrow(() -> APIException.build(HttpStatus.BAD_REQUEST, "Usuário não está bloqueado ou email não encontrado!"));
        sendEmail(user.getEmail(), EmailType.REACTIVATE);
        user.updateAccountReactivationCode();
        userRepository.save(user);
        log.debug("[finish] AuthenticationApplicationService - checkAndSendEmail");
    }

    @Override
    public void accountReactivation(String token) {
        log.info("[start] UserApplicationService - accountReactivation");
        EmailData emailData = extractPayloadFromToken(token);
        User user = userRepository.findByEmail(emailData.to())
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        log.info(emailData.code());
        user.handleAccountReactivation(emailData.code());
        userRepository.save(user);
        log.debug("[finish] UserApplicationService - accountReactivation");
    }

    @Override
    public void updateUser(UserUpdateDto userUpdateDto, String email, String token) {
        log.info("[start] UserApplicationService - updateUser");
        User user = userRepository.findByEmail(tokenService.getSubject(token))
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        assertEmailBelongsToUser(user, email);
        user.updateUser(userUpdateDto);
        userRepository.save(user);
        log.debug("[finish] UserApplicationService - updateUser");
    }

    public void updatePassword(String userId, UserPasswordUpdateDto userPasswordUpdateDto) {
        log.info("[start] UserApplicationService - updatePassword");
        generatePasswordHash(userPasswordUpdateDto);
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        log.info(user.toString());
        user.updatePassword(userPasswordUpdateDto, passwordEncoder);
        userRepository.save(user);
        log.debug("[finish] UserApplicationService - updatePassword");
    }

    private void assertEmailBelongsToUser(User user, String email) {
        if(!user.getEmail().equals(email)){
            throw APIException.build(HttpStatus.UNAUTHORIZED, "Token não corresponde ao email enviado");
        }
    }

    private void sendEmail(String userEmail, EmailType emailType) {
        log.info("[start] AuthenticationApplicationService - sendEmail");
        EmailSender payload = new EmailSender(userEmail, emailType);
        kafkaApplicationEmailProducer.publishEmail(payload);
        log.debug("[finish] AuthenticationApplicationService - sendEmail");
    }

    private EmailData extractPayloadFromToken(String token) {
        String json;
        try {
            json = tokenService.validateToken(token);
            return objectMapper.readValue(json, EmailData.class);
        } catch (Exception e) {
            throw APIException.build(HttpStatus.BAD_REQUEST, "Erro ao processar o token, verifique a validade e tente novamente.");
        }
    }

    private void generatePasswordHash(UserPasswordUpdateDto userPasswordUpdateDto) {
        log.info("[start] UserApplicationService - generatePasswordHash");
        userPasswordUpdateDto.updateHashedPassword(passwordEncoder.encode(userPasswordUpdateDto.getNewPassword()));
        log.debug("[finish] UserApplicationService - generatePasswordHash");
    }
}
