package com.ifba.sipapi.mail.application.service;

import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.mail.domain.EmailSender;
import com.ifba.sipapi.mail.domain.EmailType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class EmailStrategy {

    private final List<EmailProcess> emailProcess;

    public void emailProcess(EmailSender payload) {
        log.info("[start] EmailApplicationStrategy - emailProcess");
        EmailProcess emailApplicationProcess = strategyEmailProcess(payload.getEmailType());
        emailApplicationProcess.sendEmail(payload);
        log.debug("[finish] EmailApplicationStrategy - emailProcess");
    }

    private EmailProcess strategyEmailProcess(EmailType emailType) {
        return emailProcess.stream()
                .filter(e -> e.validateEmailType(emailType))
                .findFirst()
                .orElseThrow(() -> APIException.build(HttpStatus.BAD_REQUEST, "O tipo de email não corresponde a nenhuma estrategia"));
    }
}
