package com.ifba.sipapi.mail.application.service;

import com.ifba.sipapi.mail.domain.EmailSender;
import com.ifba.sipapi.mail.domain.EmailType;

public interface EmailProcess {
    void sendEmail(EmailSender payload);
    Boolean validateEmailType(EmailType emailType);
}
