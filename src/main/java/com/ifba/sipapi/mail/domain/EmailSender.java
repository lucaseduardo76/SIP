package com.ifba.sipapi.mail.domain;

import lombok.Getter;

@Getter
public class EmailSender {
    private final String sendTo;
    private final EmailType emailType;

    public EmailSender(String sendTo, EmailType emailType){
        this.sendTo = sendTo;
        this.emailType = emailType;
    }

}
