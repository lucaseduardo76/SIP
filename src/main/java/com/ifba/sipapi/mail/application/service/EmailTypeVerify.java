package com.ifba.sipapi.mail.application.service;

import com.ifba.sipapi.mail.domain.EmailPayloadDto;
import com.ifba.sipapi.mail.domain.EmailSender;
import com.ifba.sipapi.mail.domain.EmailType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Log4j2
@Component
public class EmailTypeVerify implements EmailProcess{

    private final EmailService emailService;

    @Override
    public void sendEmail(EmailSender payload) {
        log.info("[start] EmailTypeVerify - sendEmail");
        emailService.sendEmailWithCode(new EmailPayloadDto(
                payload.getSendTo(),
                "Veirificação de conta - SIP",
                "user/account/verify-account/"
        ));
        log.debug("[finish] EmailTypeVerify - sendEmail");
    }

    @Override
    public Boolean validateEmailType(EmailType emailType) {
        return emailType.equals(EmailType.VERIFICATION);
    }
}
