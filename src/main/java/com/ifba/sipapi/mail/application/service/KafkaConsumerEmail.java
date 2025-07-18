package com.ifba.sipapi.mail.application.service;

import com.ifba.sipapi.mail.domain.EmailSender;

public interface KafkaConsumerEmail {
    void listener(EmailSender payload);
}
