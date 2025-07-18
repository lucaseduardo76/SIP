package com.ifba.sipapi.mail.application.service;

import com.ifba.sipapi.mail.domain.EmailSender;
import com.ifba.sipapi.mail.domain.EmailType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class EmailTypeRecover implements EmailProcess{

    private final EmailService emailService;

    @Override
    public void sendEmail(EmailSender payload) {
        // Aguardando implementação
    }

    @Override
    public Boolean validateEmailType(EmailType emailType) {
        return emailType.equals(EmailType.RECOVER_PASSWORD);
    }
}
