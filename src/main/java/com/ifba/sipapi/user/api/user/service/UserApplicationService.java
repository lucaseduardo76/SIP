package com.ifba.sipapi.user.api.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.config.security.TokenService;
import com.ifba.sipapi.mail.domain.EmailSender;
import com.ifba.sipapi.mail.domain.EmailType;
import com.ifba.sipapi.mail.infra.KafkaApplicationEmailProducer;
import com.ifba.sipapi.user.domain.StatusMember;
import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.user.dto.UserAccountVerificationPayloadDto;
import com.ifba.sipapi.user.infra.UserRepository;
import com.ifba.sipapi.util.GenerateNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserApplicationService implements UserService {
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final KafkaApplicationEmailProducer kafkaApplicationEmailProducer;

    @Override
    public void verifyAccountWithToken(String token) {
        log.info("[start] AuthenticationApplicationService - verifyAccountWithToken");
        String json = tokenService.validateToken(token);
        UserAccountVerificationPayloadDto payload;
        try {
            payload = objectMapper.readValue(json, UserAccountVerificationPayloadDto.class);
        } catch (Exception e) {
            throw APIException.build(HttpStatus.BAD_REQUEST, "Token Inválido ou malformado.");
        }
        this.verifyAccount(payload);
        log.debug("[finish] AuthenticationApplicationService - verifyAccountWithToken");
    }


    @Override
    public void verifyAccount(UserAccountVerificationPayloadDto userAccountVerificationPayloadDto) {
        log.info("[start] AuthenticationApplicationService - verifyAccount");
        User user = userRepository.findByEmail(userAccountVerificationPayloadDto.getEmail())
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        user.checkVerification(userAccountVerificationPayloadDto.getVerificationCode());
        userRepository.save(user);
        log.debug("[finish] AuthenticationApplicationService - verifyAccount");
    }

    @Override
    public void resendVerificationEmail(String email) {
        log.info("[start] AuthenticationApplicationService - resendVerificationEmail");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        user.checkIfUserIsAlreadyActive();
        sendEmail(email, EmailType.VERIFICATION);
        log.debug("[finish] AuthenticationApplicationService - resendVerificationEmail");
    }

    @Override
    public void checkAndSendEmail(String email) {
        log.info("[start] AuthenticationApplicationService - checkAndSendEmail");

        User user = userRepository.findByEmail(email)
                .filter(u -> u.getStatusMember().equals(StatusMember.BLOCKED))
                .orElseThrow(() -> APIException.build(HttpStatus.BAD_REQUEST, "Usuário não está bloqueado ou email não encontrado!"));

        sendEmail(user.getEmail(), EmailType.REACTIVATE);
        log.debug("[finish] AuthenticationApplicationService - checkAndSendEmail");
    }


    private void sendEmail(String userEmail, EmailType emailType) {
        log.info("[start] AuthenticationApplicationService - sendEmail");
        EmailSender payload = new EmailSender(userEmail, emailType);
        kafkaApplicationEmailProducer.publishEmail(payload);
        log.debug("[finish] AuthenticationApplicationService - sendEmail");
    }
}
