package com.ifba.sipapi.mail.application.service;

import com.ifba.sipapi.mail.domain.EmailVerificationDTO;

public interface KafkaConsumerEmail {
    void listen(EmailVerificationDTO payload);
}
