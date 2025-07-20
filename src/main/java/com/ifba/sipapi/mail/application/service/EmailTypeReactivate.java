package com.ifba.sipapi.mail.application.service;

import com.ifba.sipapi.mail.domain.EmailSender;
import com.ifba.sipapi.mail.domain.EmailType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@RequiredArgsConstructor
public class EmailTypeReactivate implements EmailProcess{

    private final EmailService emailService;

    @Override
    public void sendEmail(EmailSender payload) {
        log.info("[start] EmailTypeReactivate - sendEmail");
        emailService.sendReactivationEmail(payload.getSendTo());
        log.debug("[finish] EmailTypeReactivate - sendEmail");
    }

    @Override
    public Boolean validateEmailType(EmailType emailType) {
        return emailType.equals(EmailType.REACTIVATE);
    }
}
